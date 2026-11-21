# 部署说明（阿里云 ACK + 托管中间件）

按架构定稿：**ACK（K8s）部署业务服务；Nacos/MySQL/Redis/ES 用阿里云托管（同一 VPC 内网）；RabbitMQ 自建（K8s StatefulSet）**。

## 一、阿里云资源准备（需要你在控制台创建，并拿到内网地址）

| 资源 | 说明 | 需要给我的信息 |
|---|---|---|
| VPC + 交换机 | 所有资源同一 VPC | 无需 |
| ACK 集群 | 托管版，2-3 可用区 | 无需 |
| MSE Nacos | 3 节点高可用 | 内网地址（`mse-xxx-p.nacos-ans.mse.aliyuncs.com:8848`） |
| RDS MySQL | 高可用版，建 6 个库 | 内网地址（`rm-xxx.mysql.rds.aliyuncs.com`） |
| Redis（Tair） | 企业版 | 内网地址（`r-xxx.redis.rds.aliyuncs.com`） |
| Elasticsearch | 托管，3 节点 | 内网地址 |
| OSS / 短信 / SLS | 对象存储/短信/日志 | 按需 |

> ⚠️ **RDS 不要开公网**，不要用 `0.0.0.0/0` 白名单；应用和 RDS 同 VPC 走内网，白名单放 VPC 网段。

## 二、初始化数据库

RDS 建好 6 个库后，执行 `docs/sql/` 下的建表脚本：

```bash
# 以 mysql 客户端为例（走内网）
mysql -h <RDS内网地址> -u <账号> -p < docs/sql/01_user_db.sql
# ... 依次执行 01~07
```

## 三、构建镜像

本地构建 jar + 镜像（以 user-service 为例）：

```bash
JAVA_HOME="C:/Program Files/Java/jdk-17" ./mvnw clean package -Dmaven.test.skip=true
docker build -f deploy/Dockerfile \
  --build-arg JAR_FILE=yunyou-user-service/target/yunyou-user-service-1.0.0-SNAPSHOT.jar \
  -t yunyou-user-service:1.0.0 .
```

推到阿里云容器镜像服务 ACR，其余 6 个服务同理。

## 四、部署

```bash
# 1. 填好中间件地址（内网）和密钥
#    deploy/k8s/configmap.yaml   —— 非敏感中间件地址
#    deploy/k8s/secret.yaml      —— 数据库/密钥/口令（CHANGE_ME 替换为真实值）
# 2. 依次 apply
kubectl apply -f deploy/k8s/configmap.yaml
kubectl apply -f deploy/k8s/secret.yaml
kubectl apply -f deploy/k8s/rabbitmq.yaml
kubectl apply -f deploy/k8s/xxl-job-admin.yaml
kubectl apply -f deploy/k8s/deployments.yaml

kubectl get pods -w   # 等所有 Pod Running
```

## 五、对外暴露

- 网关 `Service` 类型 `LoadBalancer`（阿里云分配 SLB 地址）
- 域名解析到该 SLB，SLB 前挂 WAF + CDN，HTTPS 证书在 SLB/WAF 终结

## 六、生产注意事项

- **密钥**：`secret.yaml` 里的 `CHANGE_ME` 换成强口令/随机密钥；正式环境建议用阿里云 KMS 或加密 Secret
- **RabbitMQ 延迟插件**：`rabbitmq.yaml` 用的是标准镜像，候补熔断的延迟消息需要启用 `rabbitmq_delayed_message_exchange` 插件（自建镜像加入该插件）
- **数据库口令**：不要用 root/root 之类默认值
- **HPA/灰度**：按需给核心服务（ticket/user）配 HPA；灰度用 ACK 的 canary 或网关按 header/权重路由
- **监控**：接 ARMS + Prometheus/Grafana + SLS，对账差异、MQ 堆积、429 次数等核心指标告警
