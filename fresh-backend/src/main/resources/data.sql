-- 初始化数据（仅在表为空时插入）
INSERT IGNORE INTO categories (parent_id, name, sort_order, status) VALUES
(0, '蔬菜', 1, 1),
(0, '水果', 2, 1),
(0, '肉禽蛋', 3, 1),
(0, '水产海鲜', 4, 1),
(0, '粮油干货', 5, 1),
(1, '叶菜类', 1, 1),
(1, '根茎类', 2, 1),
(1, '茄果类', 3, 1),
(2, '柑橘类', 1, 1),
(2, '浆果类', 2, 1);

-- 插入测试用户（密码: 123456, BCrypt加密）
INSERT IGNORE INTO users (phone, password_hash, nickname, user_type, status) VALUES
('13800138000', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '老王餐厅', 1, 1),
('13800138001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '学校食堂', 1, 1),
('13800138002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '社区团购', 1, 1),
('13900139000', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'XX蔬菜批发', 2, 1),
('13900139001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'YY农产品', 2, 1);

-- 插入管理员用户（密码: admin123, BCrypt加密）
INSERT IGNORE INTO users (phone, password_hash, nickname, user_type, status) VALUES
('admin', '$2a$10$OkK/zAE6hyqiyIbLVlnDB.Kl9ZHYI/d01Pz.Y/G/JknLXNDY9d/vC', '系统管理员', 3, 1);

-- 插入测试地址
INSERT IGNORE INTO user_addresses (user_id, address_type, contact_name, contact_phone, province, city, district, detail_address, longitude, latitude, is_default) VALUES
(1, 1, '王小明', '13800138000', '四川省', '成都市', '武侯区', '科华北路XX号', 104.0668, 30.5728, 1),
(2, 1, '李老师', '13800138001', '四川省', '成都市', '锦江区', 'XX街XX号', 104.0889, 30.6574, 1),
(3, 1, '张姐', '13800138002', '四川省', '成都市', '高新区', 'XX路XX号', 104.0556, 30.5367, 1);

-- 插入测试需求合并组
INSERT IGNORE INTO demand_groups (category_id, product_name, city, total_quantity, unit, merge_deadline, status) VALUES
(7, '土豆', '成都市', 450.00, '斤', '2025-12-31T18:00:00', 2),
(8, '西红柿', '成都市', 280.00, '斤', '2025-12-31T20:00:00', 2);

-- 插入测试需求
INSERT IGNORE INTO demands (group_id, buyer_id, category_id, product_name, quantity, unit, max_price, quality_requirement, delivery_address_id, delivery_date, delivery_time_slot, status) VALUES
(1, 1, 7, '土豆', 200.00, '斤', 3.50, '一级品，无发芽，单个重量150g以上', 1, '2025-12-20', '上午9-12点', 3),
(1, 2, 7, '土豆', 150.00, '斤', 3.00, '二级以上即可', 2, '2025-12-20', '上午10-12点', 3),
(1, 3, 7, '土豆', 100.00, '斤', 3.20, '一级品', 3, '2025-12-20', '下午2-5点', 3),
(2, 1, 8, '西红柿', 180.00, '斤', 5.00, '新鲜红透', 1, '2025-12-21', '上午9-12点', 3),
(2, 2, 8, '西红柿', 100.00, '斤', 4.50, '无裂果', 2, '2025-12-21', '上午10-12点', 3);

-- 插入测试报价
INSERT IGNORE INTO quotes (group_id, supplier_id, unit_price, total_amount, valid_hours, expire_at, remark, status) VALUES
(1, 4, 2.50, 1125.00, 24, '2025-12-25T18:00:00', '量大从优，品质保证', 1),
(1, 5, 2.80, 1260.00, 12, '2025-12-25T06:00:00', '本地货源，新鲜直达', 1),
(2, 4, 4.00, 1120.00, 24, '2025-12-25T18:00:00', '大棚西红柿，口感好', 1);
