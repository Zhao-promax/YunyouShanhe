# 通用服务镜像：本地 mvnw 构建好 jar 后，拷贝并运行
# 构建命令（在项目根目录执行）：
#   docker build -f deploy/Dockerfile \
#     --build-arg JAR_FILE=yunyou-user-service/target/yunyou-user-service-1.0.0-SNAPSHOT.jar \
#     -t yunyou-user-service:1.0.0 .
FROM eclipse-temurin:17-jre

ARG JAR_FILE
ENV TZ=Asia/Shanghai \
    JAVA_OPTS="-Xms256m -Xmx512m"

COPY ${JAR_FILE} /app/app.jar
WORKDIR /app
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS} -jar /app/app.jar"]
