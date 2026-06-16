# PetFoster 邻里宠物互助寄养平台

> 一个专注于邻里之间宠物互助寄养的后端服务平台，让宠物主人出差旅行时，能够放心地将爱宠托付给值得信赖的邻居。

## ✨ 功能亮点

1. **邻里互助寄养** - 连接同小区的宠物主人与爱宠邻居，替代商业化寄养，成本更低，宠物应激更小
2. **JWT安全认证** - 所有敏感接口使用JWT Token保护，确保数据安全
3. **寄养日报系统** - 寄养人每日打卡记录宠物饮食、心情、照片，主人随时了解宠物状况
4. **双向评价体系** - 寄养完成后双方互评，构建可信赖的社区信誉
5. **数据统计看板** - 平台数据总览、趋势分析、用户排行榜

## 🛠️ 技术栈

| 类别 | 技术 |
|------|------|
| 后端框架 | Java Spring Boot 3.2 |
| ORM | Spring Data JPA + Hibernate |
| 数据库 | MySQL 8.0 |
| 认证 | Spring Security + JWT |
| API文档 | SpringDoc OpenAPI (Swagger) |
| 容器化 | Docker + Docker Compose |
| 测试 | JUnit 5 |

## 📁 项目结构

```
app-14/
├── src/
│   └── main/
│       ├── java/com/petfoster/
│       │   ├── PetFosterApplication.java    # 启动类
│       │   ├── common/                       # 通用响应、异常、分页
│       │   ├── config/                       # 配置类（JWT、Security、异常处理等）
│       │   ├── controller/                   # Controller层（API入口）
│       │   ├── dto/                          # 数据传输对象
│       │   ├── entity/                       # 数据库实体
│       │   ├── repository/                   # 数据访问层
│       │   ├── service/                      # 业务逻辑层
│       │   └── util/                         # 工具类
│       └── resources/
│           └── application.yml               # 应用配置
├── docs/
│   └── functional_intro.md                   # 功能说明文档
├── postman_collection.json                   # Postman测试集合
├── Dockerfile                                # Docker镜像
├── docker-compose.yml                        # Docker编排
├── pom.xml                                   # Maven配置
├── .gitignore                                # Git忽略
└── README.md                                 # 本文件
```

## 🚀 快速启动

### 方式一：Docker 一键启动（推荐）

```bash
# 进入项目目录
cd app-14

# 一键启动（MySQL + App）
docker-compose up --build -d

# 查看应用日志
docker-compose logs -f app

# 停止服务
docker-compose down -v
```

### 方式二：本地运行

```bash
# 1. 确保已安装 JDK 17, Maven 3.9+, MySQL 8.0

# 2. 创建数据库
mysql -u root -p -e "CREATE DATABASE petfoster;"

# 3. 设置环境变量或修改 application.yml 中的数据库连接
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=petfoster
export DB_USER=your_user
export DB_PASSWORD=your_password

# 4. 启动服务
mvn spring-boot:run
```

## 🌐 访问入口

| 服务 | URL |
|------|-----|
| 健康检查 | http://localhost:8084/actuator/health |
| Swagger API文档 | http://localhost:8084/swagger-ui.html |
| OpenAPI JSON | http://localhost:8084/v3/api-docs |
| MySQL | localhost:13309 |

## 📝 API 接口清单

### 🔐 认证模块 `/api/auth`
| 方法 | 路径 | 说明 | 是否需要认证 |
|------|------|------|-------------|
| POST | /register | 用户注册 | ❌ |
| POST | /login | 用户登录 | ❌ |
| GET | /me | 获取当前用户信息 | ✅ |
| PUT | /me | 更新个人信息 | ✅ |

### 🐾 宠物模块 `/api/pets`
| 方法 | 路径 | 说明 | 是否需要认证 |
|------|------|------|-------------|
| GET | / | 获取宠物列表 | ❌ |
| GET | /mine | 获取我的宠物 | ✅ |
| GET | /{id} | 获取宠物详情 | ❌ |
| POST | / | 创建宠物 | ✅ |
| PUT | /{id} | 更新宠物 | ✅ |
| DELETE | /{id} | 删除宠物 | ✅ |

### 🏠 寄养申请模块 `/api/fosterrequests`
| 方法 | 路径 | 说明 | 是否需要认证 |
|------|------|------|-------------|
| GET | / | 获取申请列表 | ❌ |
| GET | /mine | 获取我的申请 | ✅ |
| GET | /{id} | 获取申请详情 | ❌ |
| POST | / | 创建申请 | ✅ |
| PUT | /{id} | 更新申请 | ✅ |
| PATCH | /{id}/status | 修改申请状态 | ✅ |
| DELETE | /{id} | 删除申请 | ✅ |

### 📋 寄养日报模块 `/api/fosterdailylogs`
| 方法 | 路径 | 说明 | 是否需要认证 |
|------|------|------|-------------|
| GET | / | 获取日报列表 | ❌ |
| GET | /{id} | 获取日报详情 | ❌ |
| POST | / | 创建日报 | ✅ |
| PUT | /{id} | 更新日报 | ✅ |
| DELETE | /{id} | 删除日报 | ✅ |

### ⭐ 评价模块 `/api/fosterreviews`
| 方法 | 路径 | 说明 | 是否需要认证 |
|------|------|------|-------------|
| GET | / | 获取评价列表 | ❌ |
| GET | /{id} | 获取评价详情 | ❌ |
| POST | / | 创建评价 | ✅ |
| PUT | /{id} | 更新评价 | ✅ |
| DELETE | /{id} | 删除评价 | ✅ |

### 📊 统计模块 `/api/stats`
| 方法 | 路径 | 说明 | 是否需要认证 |
|------|------|------|-------------|
| GET | /overview | 总览统计 | ❌ |
| GET | /trend | 趋势统计 | ❌ |

## 🧪 测试

### 运行单元测试

```bash
mvn test
```

### Postman 测试集合

1. 打开 Postman
2. 点击 `Import` → 选择项目根目录下的 `postman_collection.json`
3. 确保环境变量 `baseUrl` 设置为 `http://localhost:8084`
4. 运行所有请求测试接口功能

### 示例测试账号

| 用户名 | 密码 | 说明 |
|--------|------|------|
| zhangsan | 123456 | 示例用户1，有2只宠物 |
| lisi | 123456 | 示例用户2，有2只宠物 |
| wangwu | 123456 | 示例用户3，有2只宠物 |
| zhaoliu | 123456 | 示例用户4，有1只宠物 |
| sunqi | 123456 | 示例用户5，有1只宠物 |

## 📄 License

MIT License
