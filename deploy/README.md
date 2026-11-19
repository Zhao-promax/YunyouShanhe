# 部署说明（阿里云 ACK / Kubernetes）

## 1. 构建镜像

先本地构建所有服务 jar（用 JDK 17）：

```bash
JAVA_HOME="C:/Program Files/Java/jdk-17" ./mvnw clean package -Dmaven.test.skip=true
```

再逐个构建镜像（以 user-service 为例，其余同理）：

```bash
docker build -f deploy/Dockerfile \
  --build-arg JAR_FILE=yunyou-user-service/target/yunyou-user-service-1.0.0-SNAPSHOT.jar \
  -t yunyou-user-service:1.0.0 .
```

7 个服务对应关系：

| 服务 | JAR_FILE | 镜像 tag | 端口 |
|---|---|---|---|
| gateway | yunyou-gateway/target/yunyou-gateway-...jar | yunyou-gateway:1.0.0 | 8080 |
| user | yunyou-user-service/...jar | yunyou-user-service:1.0.0 | 8081 |
| content | yunyou-content-service/...jar | yunyou-content-service:1.0.0 | 8082 |
| ticket | yunyou-ticket-service/...jar | yunyou-ticket-service:1.0.0 | 8083 |
| pay | yunyou-pay-service/...jar | yunyou-pay-service:1.0.0 | 8084 |
| message | yunyou-message-service/...jar | yunyou-message-service:1.0.0 | 8085 |
| admin | yunyou-admin-service/...jar | yunyou-admin-service:1.0.0 | 8086 |

## 2. 修改中间件地址

`deploy/k8s/deployments.yaml` 里中间件地址是**占位符**（Nacos/MySQL/Redis/RabbitMQ/ES 都指向 `192.168.179.248`，即当前 VM）。

部署到 ACK 时，需要改成阿里云服务地址：
- Nacos → 阿里云 MSE Nacos 地址
- MySQL → 阿里云 RDS 内网地址（各服务 `user_db`/`content_db`/... 库）
- Redis → 阿里云 Redis 内网地址
- RabbitMQ → 自建或阿里云 AMQP 内网地址
- ES → 阿里云 ES 内网地址

## 3. 部署

```bash
kubectl apply -f deploy/k8s/deployments.yaml
kubectl get pods -w   # 等待所有 Pod Running
```

## 4. 对外暴露

网关 `Service` 类型为 `LoadBalancer`（阿里云会分配 SLB 公网/内网地址），其余服务为 `ClusterIP`（只集群内部通过 Nacos 发现访问）。

## 5. 生产注意事项

- 密钥（jwt.secret/aes.secret/渠道密钥）通过 K8s Secret 或阿里云 KMS 注入，不要硬编码
- MySQL/RabbitMQ 生产口令不要用 dev 默认值（root/root、admin/admin）
- 建议为每个服务配置 HPA（自动扩缩容）、liveness/readiness 探针、资源 limits
