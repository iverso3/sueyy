-- MySQL测试数据初始化
-- 餐厅点餐系统测试数据

-- 插入测试用户
INSERT INTO users (openid, nickname, avatar_url, role, status) VALUES
('test_openid_001', '测试用户1', 'https://example.com/avatar1.png', 'USER', 'ACTIVE'),
('test_openid_002', '测试用户2', 'https://example.com/avatar2.png', 'USER', 'ACTIVE'),
('admin_openid_001', '管理员', 'https://example.com/admin.png', 'ADMIN', 'ACTIVE');

-- 插入用户资料
INSERT INTO user_profiles (user_id, real_name, gender, birthday) VALUES
(1, '张三', 'MALE', '1990-01-01'),
(2, '李四', 'FEMALE', '1992-05-15');

-- 插入菜品分类
INSERT INTO categories (name, description, icon_url, sort_order, is_active) VALUES
('热销推荐', '最受欢迎的菜品', 'https://example.com/hot.png', 1, true),
('主食', '米饭、面条等主食', 'https://example.com/main.png', 2, true),
('小吃', '各种美味小吃', 'https://example.com/snack.png', 3, true),
('饮料', '茶饮、果汁等', 'https://example.com/drink.png', 4, true),
('套餐', '优惠套餐', 'https://example.com/combo.png', 5, true);

-- 插入菜品 (主食类)
INSERT INTO menu_items (category_id, name, description, price, original_price, image_url, is_recommended, is_hot, stock, sort_order, is_active) VALUES
(2, '红烧牛肉面', '精选牛肉，慢火炖煮，汤鲜味美', 28.00, 32.00, 'https://example.com/noodle1.jpg', true, true, 50, 1, true),
(2, '麻辣香锅', '多种食材，麻辣鲜香', 35.00, 38.00, 'https://example.com/hotpot.jpg', true, false, 30, 2, true),
(2, '宫保鸡丁饭', '经典川菜，鸡肉鲜嫩', 25.00, 28.00, 'https://example.com/chicken.jpg', false, true, 40, 3, true),
(2, '番茄鸡蛋面', '家常口味，营养丰富', 18.00, 20.00, 'https://example.com/tomato.jpg', false, false, 60, 4, true);

-- 插入菜品 (小吃类)
INSERT INTO menu_items (category_id, name, description, price, original_price, image_url, is_recommended, is_hot, stock, sort_order, is_active) VALUES
(3, '香炸鸡翅', '外酥里嫩，香气扑鼻', 15.00, 18.00, 'https://example.com/wings.jpg', true, true, 100, 1, true),
(3, '薯条', '金黄酥脆，现炸现卖', 10.00, 12.00, 'https://example.com/fries.jpg', false, true, 200, 2, true),
(3, '蔬菜沙拉', '新鲜蔬菜，健康美味', 12.00, 15.00, 'https://example.com/salad.jpg', false, false, 50, 3, true);

-- 插入菜品 (饮料类)
INSERT INTO menu_items (category_id, name, description, price, original_price, image_url, is_recommended, is_hot, stock, sort_order, is_active) VALUES
(4, '可乐', '冰镇可乐，畅爽口感', 5.00, 6.00, 'https://example.com/cola.jpg', false, true, 500, 1, true),
(4, '柠檬茶', '清新柠檬，解腻佳品', 8.00, 10.00, 'https://example.com/lemon.jpg', true, false, 200, 2, true),
(4, '鲜榨橙汁', '100%鲜榨，无添加', 12.00, 15.00, 'https://example.com/orange.jpg', true, true, 100, 3, true);

-- 插入菜品规格
INSERT INTO dish_specifications (menu_item_id, name, price_adjustment, is_default, sort_order) VALUES
(1, '标准份', 0.00, true, 1),
(1, '大份', 5.00, false, 2),
(2, '微辣', 0.00, true, 1),
(2, '中辣', 0.00, false, 2),
(2, '特辣', 0.00, false, 3),
(5, '6个装', 0.00, true, 1),
(5, '12个装', 10.00, false, 2);

-- 插入购物车数据
INSERT INTO carts (user_id, total_price, item_count) VALUES
(1, 43.00, 2),
(2, 25.00, 1);

-- 插入购物车项
INSERT INTO cart_items (cart_id, menu_item_id, quantity, price, subtotal) VALUES
(1, 1, 1, 28.00, 28.00),
(1, 5, 1, 15.00, 15.00),
(2, 3, 1, 25.00, 25.00);

-- 插入订单数据
INSERT INTO orders (order_no, user_id, total_amount, discount_amount, actual_amount, status, payment_method, payment_status, payment_time) VALUES
('ORDER202501010001', 1, 43.00, 3.00, 40.00, 'COMPLETED', 'WECHAT', 'PAID', '2025-01-01 12:30:00'),
('ORDER202501010002', 2, 25.00, 0.00, 25.00, 'PREPARING', 'WECHAT', 'PAID', '2025-01-01 13:15:00'),
('ORDER202501010003', 1, 18.00, 0.00, 18.00, 'PENDING', 'WECHAT', 'UNPAID', NULL);

-- 插入订单项
INSERT INTO order_items (order_id, menu_item_id, menu_item_name, menu_item_price, quantity, subtotal) VALUES
(1, 1, '红烧牛肉面', 28.00, 1, 28.00),
(1, 5, '香炸鸡翅', 15.00, 1, 15.00),
(2, 3, '宫保鸡丁饭', 25.00, 1, 25.00),
(3, 4, '番茄鸡蛋面', 18.00, 1, 18.00);

-- 插入管理员账号 (密码: admin123)
INSERT INTO users (openid, nickname, role, status) VALUES
('admin', '系统管理员', 'ADMIN', 'ACTIVE');

INSERT INTO user_profiles (user_id, real_name) VALUES
(4, '系统管理员');