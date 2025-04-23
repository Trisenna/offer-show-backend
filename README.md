# Offer Show 后台服务系统

## 项目介绍

Offer Show 是一个分享、展示和比较职位 offer 信息的平台，本项目是其后台服务系统，提供了 Offer 信息的管理、检索、统计分析等功能。

## 功能特性

- **Offer管理**：创建、查询、更新和删除Offer信息
- **搜索功能**：支持关键词搜索和分页查询
- **批处理功能**：支持批量创建和删除Offer
- **导入导出**：支持导入Excel表格和导出CSV/Excel格式数据
- **统计分析**：支持按公司、职位、城市等维度的薪资统计和趋势分析
- **定时任务**：自动生成统计报表和清理过期数据

## 技术栈

- **后端框架**：Spring Boot 2.7.x
- **ORM框架**：MyBatis
- **数据库**：MySQL 8.0
- **任务调度**：Spring Scheduler + Quartz
- **API文档**：Swagger
- **容器化**：Docker + Docker Compose

## 系统架构

- **表示层**：RESTful API 接口，负责接收和响应 HTTP 请求
- **业务逻辑层**：实现核心业务逻辑和处理
- **数据访问层**：负责与数据库的交互
- **基础设施层**：提供日志、缓存、消息队列等基础服务

## 快速开始

### 环境要求

- JDK 11+
- Maven 3.6+
- MySQL 8.0+
- Docker & Docker Compose (可选，用于容器化部署)

### 本地开发

1. 克隆项目到本地

```bash
git clone https://github.com/Trisenna/offer_show_backend.git
cd offer-show-backend
```

2. 创建数据库和表

```bash
mysql -u root -p < schema.sql
```

3. 修改配置

编辑 `src/main/resources/application-dev.yml` 文件，配置正确的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/offer_show?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
```

4. 构建并运行

```bash
mvn clean package
java -jar target/offer-show-backend-0.0.1-SNAPSHOT.jar
```

5. 访问API文档

在浏览器中访问 [http://localhost:8080/api/swagger-ui/](http://localhost:8080/api/swagger-ui/) 查看API文档。

### Docker部署

1. 构建项目

```bash
mvn clean package
```

2. 使用Docker Compose启动服务

```bash
docker-compose up -d
```

3. 访问API文档

在浏览器中访问 [http://localhost:8080/api/swagger-ui/](http://localhost:8080/api/swagger-ui/) 查看API文档。

## API接口

系统提供了一系列RESTful API接口，主要包括：

### Offer管理

- `POST /api/v1/offers` - 创建Offer
- `GET /api/v1/offers/{id}` - 获取Offer详情
- `PUT /api/v1/offers/{id}` - 更新Offer
- `PATCH /api/v1/offers/{id}` - 部分更新Offer
- `DELETE /api/v1/offers/{id}` - 删除Offer
- `GET /api/v1/offers/search` - 搜索Offer

### 批处理

- `POST /api/v1/offers/batch` - 批量创建Offer
- `DELETE /api/v1/offers/batch` - 批量删除Offer

### 导入导出

- `POST /api/v1/offers/export` - 导出Offer数据
- `GET /api/v1/export-tasks/{taskId}` - 获取导出任务状态
- `POST /api/v1/offers/import` - 导入Offer数据

### 统计分析

- `GET /api/v1/statistics/salary` - 获取薪资统计数据
- `GET /api/v1/statistics/trend` - 获取趋势统计数据

## 测试

项目提供了完整的测试脚本，可以用于测试各个API接口的功能：

```bash
./test_api.sh
```
或者
```
offer-show-backend\src\test\java\com\offershow\test\OfferShowAPITest.java
```

## 项目目录结构

```
offer-show-backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── offershow/
│   │   │           ├── OfferShowApplication.java
│   │   │           ├── config/
│   │   │           ├── controller/
│   │   │           ├── service/
│   │   │           ├── repository/
│   │   │           ├── model/
│   │   │           ├── task/
│   │   │           ├── exception/
│   │   │           └── util/
│   │   └── resources/
│   └── test/
├── schema.sql
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

## 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建Pull Request

## 许可证

本项目采用 MIT 许可证，详情请参阅 [LICENSE](LICENSE) 文件。

## 联系方式

如有任何问题或建议，可以通过以下方式联系我：

- 电子邮件：2834136003@qq.com
