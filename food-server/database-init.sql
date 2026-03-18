-- ============================================================
-- 餐厅点餐系统数据库初始化脚本
-- 数据库: MySQL
-- 创建时间: 2026-03-18
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS restaurant_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE restaurant_db;

-- ============================================================
-- 1. 用户管理相关表
-- ============================================================

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    openid VARCHAR(128) UNIQUE NOT NULL COMMENT '微信openid',
    unionid VARCHAR(128) COMMENT '微信unionid',
    nickname VARCHAR(100) COMMENT '用户昵称',
    avatar_url VARCHAR(500) COMMENT '头像URL',
    phone VARCHAR(20) COMMENT '手机号',
    role ENUM('USER', 'ADMIN') DEFAULT 'USER' NOT NULL COMMENT '用户角色',
    status ENUM('ACTIVE', 'INACTIVE', 'BANNED') DEFAULT 'ACTIVE' NOT NULL COMMENT '用户状态',
    last_login_time DATETIME COMMENT '最后登录时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    INDEX idx_openid (openid),
    INDEX idx_phone (phone),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 用户资料表
CREATE TABLE IF NOT EXISTS user_profiles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    real_name VARCHAR(50) COMMENT '真实姓名',
    gender ENUM('MALE', 'FEMALE', 'UNKNOWN') DEFAULT 'UNKNOWN' COMMENT '性别',
    birthday DATE COMMENT '生日',
    preferences JSON COMMENT '用户偏好设置',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户资料表';

-- ============================================================
-- 2. 菜品管理相关表
-- ============================================================

-- 菜品分类表
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    description VARCHAR(500) COMMENT '分类描述',
    icon_url VARCHAR(500) COMMENT '分类图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    is_active BOOLEAN DEFAULT TRUE NOT NULL COMMENT '是否启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    INDEX idx_sort_order (sort_order),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜品分类表';

-- 菜品表
CREATE TABLE IF NOT EXISTS menu_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id BIGINT NOT NULL COMMENT '分类ID',
    name VARCHAR(100) NOT NULL COMMENT '菜品名称',
    description VARCHAR(1000) COMMENT '菜品描述',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    original_price DECIMAL(10,2) COMMENT '原价',
    image_url VARCHAR(500) COMMENT '图片URL',
    is_recommended BOOLEAN DEFAULT FALSE COMMENT '是否推荐',
    is_hot BOOLEAN DEFAULT FALSE COMMENT '是否热销',
    stock INT DEFAULT -1 COMMENT '库存(-1表示无限)',
    sort_order INT DEFAULT 0 COMMENT '排序',
    is_active BOOLEAN DEFAULT TRUE NOT NULL COMMENT '是否启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (category_id) REFERENCES categories(id),
    INDEX idx_category_id (category_id),
    INDEX idx_is_active (is_active),
    INDEX idx_is_recommended (is_recommended),
    INDEX idx_is_hot (is_hot),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜品表';

-- 菜品规格表
CREATE TABLE IF NOT EXISTS dish_specifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    menu_item_id BIGINT NOT NULL COMMENT '菜品ID',
    name VARCHAR(100) NOT NULL COMMENT '规格名称',
    price_adjustment DECIMAL(10,2) DEFAULT 0.00 COMMENT '价格调整',
    is_default BOOLEAN DEFAULT FALSE COMMENT '是否默认规格',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE,
    INDEX idx_menu_item_id (menu_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜品规格表';

-- ============================================================
-- 3. 购物车相关表
-- ============================================================

-- 购物车表
CREATE TABLE IF NOT EXISTS carts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    total_price DECIMAL(10,2) DEFAULT 0.00 COMMENT '总价',
    item_count INT DEFAULT 0 COMMENT '商品数量',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';

-- 购物车项表
CREATE TABLE IF NOT EXISTS cart_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cart_id BIGINT NOT NULL COMMENT '购物车ID',
    menu_item_id BIGINT NOT NULL COMMENT '菜品ID',
    specification_id BIGINT COMMENT '规格ID',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    price DECIMAL(10,2) NOT NULL COMMENT '单价',
    subtotal DECIMAL(10,2) NOT NULL COMMENT '小计',
    image_url VARCHAR(500) COMMENT '菜品图片URL',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id),
    FOREIGN KEY (specification_id) REFERENCES dish_specifications(id),
    INDEX idx_cart_id (cart_id),
    INDEX idx_menu_item_id (menu_item_id),
    UNIQUE KEY uk_cart_menu_spec (cart_id, menu_item_id, specification_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车项表';

-- ============================================================
-- 4. 订单相关表
-- ============================================================

-- 订单表
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(32) UNIQUE NOT NULL COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    discount_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '优惠金额',
    actual_amount DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    status ENUM('PENDING', 'PAID', 'PREPARING', 'READY', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING' NOT NULL COMMENT '订单状态',
    payment_method ENUM('WECHAT', 'ALIPAY', 'CASH') DEFAULT 'WECHAT' COMMENT '支付方式',
    payment_status ENUM('UNPAID', 'PAID', 'REFUNDED') DEFAULT 'UNPAID' COMMENT '支付状态',
    payment_time DATETIME COMMENT '支付时间',
    pickup_time DATETIME COMMENT '取餐时间',
    remark VARCHAR(500) COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_order_no (order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_payment_status (payment_status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 订单项表
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    menu_item_id BIGINT NOT NULL COMMENT '菜品ID',
    menu_item_name VARCHAR(100) NOT NULL COMMENT '菜品名称(快照)',
    menu_item_price DECIMAL(10,2) NOT NULL COMMENT '菜品价格(快照)',
    specification_name VARCHAR(100) COMMENT '规格名称(快照)',
    specification_price_adjustment DECIMAL(10,2) DEFAULT 0.00 COMMENT '规格价格调整(快照)',
    quantity INT NOT NULL COMMENT '数量',
    subtotal DECIMAL(10,2) NOT NULL COMMENT '小计',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id),
    INDEX idx_order_id (order_id),
    INDEX idx_menu_item_id (menu_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单项表';

-- ============================================================
-- 5. 支付相关表
-- ============================================================

-- 支付记录表
CREATE TABLE IF NOT EXISTS payment_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    payment_no VARCHAR(64) UNIQUE NOT NULL COMMENT '支付平台流水号',
    transaction_id VARCHAR(64) COMMENT '微信支付交易号',
    amount DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    payment_method ENUM('WECHAT', 'ALIPAY', 'CASH') NOT NULL COMMENT '支付方式',
    status ENUM('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED') DEFAULT 'PENDING' NOT NULL COMMENT '支付状态',
    pay_time DATETIME COMMENT '支付时间',
    callback_data TEXT COMMENT '回调数据',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    INDEX idx_order_id (order_id),
    INDEX idx_payment_no (payment_no),
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';

-- ============================================================
-- 6. 评价相关表
-- ============================================================

-- 菜品评价表
CREATE TABLE IF NOT EXISTS dish_reviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_item_id BIGINT NOT NULL COMMENT '订单项ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    menu_item_id BIGINT NOT NULL COMMENT '菜品ID',
    rating TINYINT NOT NULL CHECK (rating >= 1 AND rating <= 5) COMMENT '评分(1-5)',
    comment TEXT COMMENT '评价内容',
    images JSON COMMENT '评价图片',
    is_anonymous BOOLEAN DEFAULT FALSE COMMENT '是否匿名',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (order_item_id) REFERENCES order_items(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id),
    UNIQUE KEY uk_order_item_id (order_item_id),
    INDEX idx_menu_item_id (menu_item_id),
    INDEX idx_user_id (user_id),
    INDEX idx_rating (rating)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜品评价表';

-- ============================================================
-- 7. 系统管理相关表
-- ============================================================

-- 管理员表
CREATE TABLE IF NOT EXISTS admins (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    nickname VARCHAR(100) COMMENT '昵称',
    role ENUM('SUPER_ADMIN', 'ADMIN', 'STAFF') DEFAULT 'STAFF' COMMENT '角色',
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE' NOT NULL COMMENT '状态',
    last_login_time DATETIME COMMENT '最后登录时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    INDEX idx_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS system_configs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(100) UNIQUE NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_type ENUM('STRING', 'NUMBER', 'BOOLEAN', 'JSON', 'TEXT') DEFAULT 'STRING' COMMENT '配置类型',
    description VARCHAR(500) COMMENT '描述',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    INDEX idx_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- ============================================================
-- 8. 初始化默认数据
-- ============================================================

-- 插入默认管理员账号 (密码: admin123)
INSERT IGNORE INTO admins (username, password, nickname, role, status)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lOBslK7l2Q5FQK', '系统管理员', 'SUPER_ADMIN', 'ACTIVE');

-- 插入默认分类
INSERT IGNORE INTO categories (name, description, sort_order) VALUES
('热销推荐', '最受欢迎的菜品', 1),
('主食', '米饭、面条等主食', 2),
('小吃', '各种美味小吃', 3),
('饮料', '茶饮、果汁等', 4),
('套餐', '优惠套餐', 5);

-- 插入系统配置
INSERT IGNORE INTO system_configs (config_key, config_value, config_type, description) VALUES
('business_hours', '{"open": "09:00", "close": "22:00"}', 'JSON', '营业时间'),
('min_order_amount', '20.00', 'NUMBER', '最低起送金额'),
('pickup_notice', '请凭订单号到前台取餐', 'STRING', '取餐提示'),
('contact_phone', '13800138000', 'STRING', '联系电话');

-- ============================================================
-- 初始化完成
-- ============================================================
