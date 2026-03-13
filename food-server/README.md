# 线上点菜微信小程序后端

基于Spring Boot 3.x + Java 17的线上点菜微信小程序后端系统。

## 功能特性

- **用户管理**: 微信登录、用户信息管理
- **菜品管理**: 菜品分类、列表、搜索、详情
- **购物车**: 添加商品、修改数量、清空购物车
- **订单管理**: 创建订单、订单状态管理、历史查询
- **支付集成**: 微信支付集成（支持扩展其他支付方式）
- **后台管理**: 菜品管理、订单管理、用户管理
- **缓存优化**: Redis缓存菜品信息、购物车数据

## 技术栈

- **后端框架**: Spring Boot 3.2.0
- **Java版本**: 17
- **数据库**: MySQL 8.0 + Redis 7.x
- **数据访问**: Spring Data JPA
- **安全认证**: Spring Security + JWT
- **API文档**: SpringDoc OpenAPI 3
- **构建工具**: Maven
- **容器化**: Docker + Docker Compose

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- Docker 20.10+ (推荐，用于MySQL+Redis)
- MySQL 8.0 (如果不用Docker)
- Redis 7.x (如果不用Docker)
- **H2内存数据库** (无需安装，快速测试)

### 使用Docker Compose启动

1. 克隆项目并进入目录
   ```bash
   cd food-server
   ```

2. 启动所有服务
   ```bash
   docker-compose up -d
   ```

3. 访问应用
   - 应用: http://localhost:8080/api
   - Swagger文档: http://localhost:8080/swagger-ui.html
   - MySQL: localhost:3306
   - Redis: localhost:6379

### H2内存数据库模式（快速测试）

无需安装任何外部数据库，使用内置H2内存数据库进行快速测试：

1. **启动应用**：
   ```bash
   cd food-server
   # Windows
   start-h2.bat
   # 或手动启动
   mvn spring-boot:run -Dspring-boot.run.profiles=h2
   ```

2. **访问地址**：
   - 应用API: http://localhost:8080/api
   - Swagger文档: http://localhost:8080/swagger-ui.html
   - H2控制台: http://localhost:8080/h2-console
     - JDBC URL: `jdbc:h2:mem:restaurant_db`
     - 用户名: `sa`
     - 密码: (空)

3. **测试数据**：
   - 系统已预置测试数据（用户、菜品、订单等）
   - 默认用户：微信登录使用模拟openid

4. **注意事项**：
   - 数据存储在内存中，应用重启后数据丢失
   - 使用简单内存缓存（非Redis）
   - 适合快速测试和开发

### 手动启动（MySQL+Redis）

1. 启动MySQL和Redis服务
2. 修改配置文件 `src/main/resources/application.yml`
3. 构建并运行应用
   ```bash
   mvn clean package
   java -jar target/restaurant-ordering-0.0.1-SNAPSHOT.jar
   ```

## 项目结构

```
src/main/java/com/restaurant/ordering/
├── config/                    # 配置类
├── controller/               # 控制器层
│   ├── api/                  # 小程序API
│   └── admin/                # 后台管理API
├── service/                  # 服务层
│   └── impl/                 # 服务实现
├── repository/               # 数据访问层
├── model/                    # 数据模型
│   ├── entity/               # JPA实体
│   ├── dto/                  # 数据传输对象
│   └── enums/                # 枚举类
├── security/                 # 安全相关
├── exception/                # 异常处理
├── util/                     # 工具类
└── RestaurantOrderingApplication.java
```

## API接口

### 认证相关
- `POST /api/auth/login` - 微信登录
- `POST /api/auth/logout` - 登出

### 菜品相关
- `GET /api/menu/categories` - 获取分类列表
- `GET /api/menu/items` - 获取菜品列表
- `GET /api/menu/items/{id}` - 获取菜品详情
- `GET /api/menu/items/recommended` - 获取推荐菜品
- `GET /api/menu/items/hot` - 获取热销菜品
- `GET /api/menu/items/search` - 搜索菜品

### 购物车相关
- `GET /api/cart` - 获取购物车
- `POST /api/cart/items` - 添加商品到购物车
- `PUT /api/cart/items/{id}` - 更新购物车商品数量
- `DELETE /api/cart/items/{id}` - 删除购物车商品
- `DELETE /api/cart/clear` - 清空购物车

### 订单相关
- `POST /api/orders` - 创建订单
- `GET /api/orders` - 获取订单列表
- `GET /api/orders/{id}` - 获取订单详情
- `PUT /api/orders/{id}/cancel` - 取消订单

### 支付相关
- `POST /api/payment/prepay` - 生成预支付订单
- `POST /api/payment/query/{orderNo}` - 查询支付状态
- `POST /api/payment/callback` - 支付回调(微信调用)

## 数据库设计

主要数据表：
- `users` - 用户表
- `user_profiles` - 用户资料表
- `categories` - 菜品分类表
- `menu_items` - 菜品表
- `dish_specifications` - 菜品规格表
- `carts` - 购物车表
- `cart_items` - 购物车项表
- `orders` - 订单表
- `order_items` - 订单项表
- `payment_records` - 支付记录表
- `dish_reviews` - 菜品评价表
- `admins` - 管理员表
- `system_configs` - 系统配置表

## 配置说明

### 配置文件
- `application.yml` - 主配置文件
- `application-dev.yml` - 开发环境配置
- `application-prod.yml` - 生产环境配置
- `application-docker.yml` - Docker环境配置

### 环境变量
- `DB_HOST` - 数据库主机
- `DB_PORT` - 数据库端口
- `DB_NAME` - 数据库名称
- `DB_USERNAME` - 数据库用户名
- `DB_PASSWORD` - 数据库密码
- `REDIS_HOST` - Redis主机
- `REDIS_PORT` - Redis端口
- `REDIS_PASSWORD` - Redis密码
- `JWT_SECRET` - JWT密钥
- `WECHAT_APP_ID` - 微信小程序AppID
- `WECHAT_APP_SECRET` - 微信小程序Secret
- `WECHAT_MCH_ID` - 微信商户号
- `WECHAT_MCH_KEY` - 微信商户密钥

## 开发指南

### 添加新的API
1. 在`controller/api/`或`controller/admin/`下创建新的Controller
2. 在`service/`下创建Service接口
3. 在`service/impl/`下实现Service
4. 在`repository/`下创建Repository接口
5. 在`model/`下创建相关的实体类和DTO

### 数据库迁移
- 开发环境: JPA自动更新表结构 (`ddl-auto: update`)
- 生产环境: 使用Flyway或手动执行SQL脚本

### 测试
```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=AuthControllerTest
```

## 部署

### Docker部署
```bash
# 构建镜像
docker build -t restaurant-ordering:latest .

# 运行容器
docker run -d -p 8080:8080 --name restaurant-app restaurant-ordering:latest
```

### Kubernetes部署
参考 `kubernetes/` 目录下的YAML文件（待补充）

## 常见问题

1. **微信登录失败**
   - 检查微信小程序配置
   - 确认`WECHAT_APP_ID`和`WECHAT_APP_SECRET`正确

2. **数据库连接失败**
   - 检查MySQL服务是否启动
   - 确认数据库连接配置正确

3. **Redis连接失败**
   - 检查Redis服务是否启动
   - 确认Redis连接配置正确

4. **JWT认证失败**
   - 检查请求头中是否包含有效的Token
   - 确认JWT密钥配置正确

## 联系方式

如有问题或建议，请联系：
- 邮箱: support@restaurant.com
- 项目地址: [GitHub Repository](https://github.com/your-repo/restaurant-ordering)

## 许可证

Apache License 2.0