-- MySQL数据库初始化脚本
-- 需要在MySQL中执行此脚本（使用root权限）
-- 请替换 'your_mysql_root_password' 为实际的MySQL root密码

-- 创建数据库
CREATE DATABASE IF NOT EXISTS restaurant_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- 创建用户并授权
CREATE USER IF NOT EXISTS 'restaurant_user'@'localhost' IDENTIFIED BY 'restaurant_password';
GRANT ALL PRIVILEGES ON restaurant_db.* TO 'restaurant_user'@'localhost';
FLUSH PRIVILEGES;

-- 使用新创建的数据库
USE restaurant_db;

-- 注意：表结构将由应用程序通过schema-mysql.sql和data-mysql.sql文件自动创建
-- 应用程序启动时会自动执行这些SQL脚本