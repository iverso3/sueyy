@echo off
echo ========================================
echo 线上点菜微信小程序后端 - H2内存数据库模式
echo ========================================
echo.

echo 正在启动应用（使用H2内存数据库）...
echo.

echo 应用启动后可通过以下地址访问：
echo 1. 应用API: http://localhost:8080/api
echo 2. Swagger文档: http://localhost:8080/swagger-ui.html
echo 3. H2数据库控制台: http://localhost:8080/h2-console
echo    - JDBC URL: jdbc:h2:mem:restaurant_db
echo    - 用户名: sa
echo    - 密码: (空)
echo.

echo 正在编译和启动应用...
echo.

call mvn clean spring-boot:run -Dspring-boot.run.profiles=h2

if %errorlevel% neq 0 (
    echo.
    echo 启动失败！请检查错误信息。
    pause
    exit /b 1
)