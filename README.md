# 云游山河（YunyouShanhe）

景区预约候补与目的地攻略平台 —— 后端微服务。

> 架构定稿见 [架构设计.md](架构设计.md)，业务方向见 [plan.md](plan.md)。

## 技术栈

- Java 17、Spring Boot 3.2.x、Spring Cloud 2023.0.x、Spring Cloud Alibaba 2023.0.x
- Nacos（注册发现）、Spring Cloud Gateway、OpenFeign、Sentinel（预留）
- RabbitMQ（异步事件）、Redis（缓存 / 候补队列）、MySQL 8（每服务独立库）、Elasticsearch 8（检索）
- MyBatis-Plus、XXL-Job（预留）、JWT、BCrypt、AES-GCM
- 阿里云 OSS / 短信 / SLS（生产，当前开发期为 Mock）

## 模块结构

| 模块 | 端口 | 职责 |
|---|---|---|
| yunyou-gateway | 8080 | 路由、限流、CORS、鉴权（占位） |
| yunyou-common | — | 统一响应/异常、MQ 常量、事件定义 |
| yunyou-user-service | 8081 | 用户、实名、认证 |
| yunyou-content-service | 8082 | 城市/景点/攻略、检索、推荐 |
| yunyou-ticket-service | 8083 | 现货购票、候补队列、退票（核心） |
| yunyou-pay-service | 8084 | 支付、退款、资金台账 |
| yunyou-message-service | 8085 | 通知、站内信、订阅 |
| yunyou-admin-service | 8086 | 运营账号、工单、审计 |

## 快速开始

### 前置要求

- **JDK 17**（本机若有多套 JDK，构建/运行请使用 JDK 17；`JAVA_HOME` 指向 JDK 17）
- **Maven**：通过仓库自带的 Maven Wrapper（`mvnw`）使用，无需单独安装
- **Docker**：本地中间件（Nacos/Redis/RabbitMQ/MySQL/ES）通过 `docker-compose.yml` 启动

### 启动中间件

```bash
docker compose up -d
```

中间件地址（默认本机）：

| 组件 | 端口 |
|---|---|
| Nacos | 8848（gRPC 9848） |
| Redis | 6379 |
| RabbitMQ | 5672（管理台 15672，admin/admin） |
| MySQL | 3306（root/root） |
| Elasticsearch | 9200 |

> 各服务 `application.yml` 已配置中间件地址为虚拟机 IP `192.168.179.248`（若改用本机 Docker，改回 `127.0.0.1` 即可）。

### 构建

```bash
# Windows（Git Bash）
JAVA_HOME="C:/Program Files/Java/jdk-17" ./mvnw clean package -Dmaven.test.skip=true
```

### 运行

```bash
# 方式一：Maven 运行单个服务
JAVA_HOME="C:/Program Files/Java/jdk-17" ./mvnw -pl yunyou-user-service spring-boot:run

# 方式二：运行打包后的 jar
java -jar yunyou-user-service/target/yunyou-user-service-1.0.0-SNAPSHOT.jar
```

## 接口清单

鉴权约定：C 端服务通过请求头 `X-User-Id` 传递用户身份（网关 JWT 鉴权接入后由网关注入）；用户登录接口返回 `Authorization: Bearer <token>`。

### 用户服务 `/api/user`

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /auth/sms/send | 发送短信验证码（Mock） |
| POST | /auth/register | 注册 |
| POST | /auth/login/password | 密码登录 |
| POST | /auth/login/sms | 短信登录 |
| POST | /auth/logout | 登出 |
| GET | /auth/me | 当前用户 |
| POST | /realname | 新增实名档案 |
| GET | /realname/list | 实名列表 |
| DELETE | /realname/{id} | 删除实名 |

### 内容服务 `/api/content`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /city/list | 城市列表 |
| GET | /scenic/list | 景点列表（cityId/category/season/budgetLevel/minRating） |
| GET | /scenic/{id} | 景点详情 |
| POST | /scenic | 新增/更新景点 |
| GET | /scenic/nearby | 同片区顺路推荐 |
| GET | /scenic/{id}/rule | 景点预约规则 |
| POST | /scenic/rule | 保存预约规则 |
| GET | /guide/list | 攻略列表（scenicId/cityId/season/budgetLevel/guideType） |
| GET | /guide/{id} | 攻略详情 |
| POST | /guide | 新增/更新攻略 |
| GET | /search/scenic | ES 景点检索 |
| GET | /search/guide | ES 攻略检索 |

### 票务服务 `/api/ticket`

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /book | 现货下单 |
| POST | /waitlist | 候补登记 |
| POST | /waitlist/cancel | 取消候补 |
| GET | /order/{orderNo} | 订单查询 |
| POST | /order/{orderNo}/confirm-pay | 支付确认（Mock） |
| POST | /order/{orderNo}/refund | 退票 |
| POST | /order/{orderNo}/close | 超时关闭（内部） |
| POST | /waitlist/cutoff | 候补熔断（内部） |
| POST | /webhook/inventory | 景区库存释放回调 |

### 支付服务 `/api/pay`

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /create | 创建支付单 |
| POST | /callback/mock | 支付回调（Mock） |
| GET | /order/{payOrderNo} | 支付单查询 |

### 消息服务 `/api/message`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /inbox | 站内信收件箱 |
| POST | /{id}/read | 标记已读 |
| POST | /subscribe | 景点订阅 |
| POST | /unsubscribe | 退订 |

### 运营服务 `/api/admin`

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /auth/login | 运营账号登录 |
| GET | /work-order/list | 工单列表 |
| POST | /work-order/{id}/handle | 处理工单 |
| GET | /audit/list | 审计日志 |

## 环境变量注意事项

- 本机若同时存在 JDK 8/11/17，构建和运行务必使用 **JDK 17**（`JAVA_HOME` 或运行命令显式指定）。
- 开发期密钥（JWT/AES）在各服务 `application.yml` 里为占位值，上线前迁移到 Nacos 配置中心 / 阿里云 KMS。

## 已知 TODO

见 [架构设计.md](架构设计.md) 的「风险与前置确认项」，以及代码中 `TODO` 注释：网关 JWT 鉴权、微信登录/短信/推送真实现、Webhook 验签、Sentinel 限流、三方对账、票种价格、队列补偿与 XXL-Job、MQ 手动 ACK/死信/幂等。
