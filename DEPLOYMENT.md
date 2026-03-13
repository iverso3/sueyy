# 部署配置说明

## 后端部署配置 (food-server/deployment.yaml)

部署前需要修改以下配置：

### 1. 修改数据库密码
```yaml
- name: DB_PASSWORD
  value: "your_db_password"  # 改为强密码
```

### 2. 修改Redis密码
```yaml
- name: REDIS_PASSWORD
  value: "your_redis_password"
```

### 3. 修改JWT密钥
```yaml
- name: JWT_SECRET
  value: "your_jwt_secret_key"  # 建议使用随机字符串
```

### 4. 修改微信小程序配置
```yaml
- name: WECHAT_APPID
  value: "your_wechat_appid"
- name: WECHAT_SECRET
  value: "your_wechat_secret"
```

### 5. 修改域名
```yaml
- host: api.your-domain.com  # 改为你的实际域名
```

## 小程序前端配置 (food-miniapp/app.js)

部署前修改第9行：
```javascript
apiBaseUrl: 'http://localhost:8080/api',  // 开发环境
// 改为:
apiBaseUrl: 'https://你的域名/api',  // 生产环境
```

## 快速部署命令

### 1. 构建Docker镜像
```bash
cd food-server
docker build -t iverso3/sueyy-food-server:latest .
docker push iverso3/sueyy-food-server:latest
```

### 2. Sealos部署
在Sealos云控制台：
1. 创建新应用
2. 填写镜像: iverso3/sueyy-food-server:latest
3. 配置环境变量
4. 部署

### 3. 小程序提交审核
1. 登录微信公众平台
2. 配置服务器域名
3. 上传代码
4. 提交审核
