FROM openjdk:11-jdk-slim

WORKDIR /app

# 添加时区支持
RUN apt-get update && apt-get install -y tzdata
ENV TZ=Asia/Shanghai

# 创建导出目录
RUN mkdir -p /app/exports

# 复制JAR包
COPY target/offer-show-backend-0.0.1-SNAPSHOT.jar /app/app.jar

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "/app/app.jar"]