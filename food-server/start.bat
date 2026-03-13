@echo off
echo 启动线上点菜微信小程序后端服务
echo.

echo 1. 使用Docker Compose启动所有服务
echo 2. 手动启动Spring Boot应用
echo 3. 退出
echo.

set /p choice="请选择启动方式 (1/2/3): "

if "%choice%"=="1" (
    echo 启动Docker Compose服务...
    docker-compose up -d
    echo 服务启动完成！
    echo 应用地址: http://localhost:8080/api
    echo Swagger文档: http://localhost:8080/swagger-ui.html
    echo MySQL: localhost:3306 (用户名: restaurant_user, 密码: restaurant_password)
    echo Redis: localhost:6379
    pause
) else if "%choice%"=="2" (
    echo 构建并启动Spring Boot应用...
    call mvn clean package -DskipTests
    if %errorlevel% neq 0 (
        echo 构建失败！
        pause
        exit /b 1
    )
    java -jar target/restaurant-ordering-0.0.1-SNAPSHOT.jar
) else if "%choice%"=="3" (
    echo 退出
    exit /b 0
) else (
    echo 无效的选择
    pause
)