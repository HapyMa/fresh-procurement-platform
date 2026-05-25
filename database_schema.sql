-- ============================================================
-- 生鲜采购平台数据库设计
-- 技术栈：PostgreSQL / MySQL 兼容
-- 设计原则：支持需求合并、拆单履约、供应商分拣打包
-- ============================================================

-- ------------------------------------------------------------
-- 1. 用户模块
-- ------------------------------------------------------------

-- 用户基础表（供应商 & 采购商统一存储）
CREATE TABLE users (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    phone           VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号（登录账号）',
    password_hash   VARCHAR(255) NOT NULL COMMENT '密码哈希',
    nickname        VARCHAR(50) COMMENT '昵称/企业名称',
    avatar_url      VARCHAR(500) COMMENT '头像URL',
    user_type       TINYINT NOT NULL COMMENT '用户类型：1-采购商 2-供应商 3-两者皆是',
    status          TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-正常 2-待审核',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_phone (phone),
    INDEX idx_user_type (user_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户基础表';

-- 用户认证信息（资质认证）
CREATE TABLE user_verifications (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id             BIGINT NOT NULL COMMENT '用户ID',
    verification_type   TINYINT NOT NULL COMMENT '认证类型：1-企业认证 2-个人认证',
    real_name           VARCHAR(100) COMMENT '真实姓名/法人姓名',
    id_card_no          VARCHAR(18) COMMENT '身份证号',
    business_name       VARCHAR(200) COMMENT '企业名称',
    business_license_no VARCHAR(50) COMMENT '营业执照号',
    business_license_url VARCHAR(500) COMMENT '营业执照图片',
    food_license_url    VARCHAR(500) COMMENT '食品经营许可证',
    verify_status       TINYINT DEFAULT 0 COMMENT '认证状态：0-未认证 1-审核中 2-已通过 3-已拒绝',
    reject_reason       VARCHAR(500) COMMENT '拒绝原因',
    verified_at         TIMESTAMP COMMENT '认证通过时间',
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_user_id (user_id),
    INDEX idx_verify_status (verify_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户认证信息';

-- 用户地址表（采购商收货地址 / 供应商发货地址）
CREATE TABLE user_addresses (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT NOT NULL COMMENT '用户ID',
    address_type    TINYINT NOT NULL COMMENT '地址类型：1-收货地址 2-发货地址',
    contact_name    VARCHAR(50) COMMENT '联系人姓名',
    contact_phone   VARCHAR(20) COMMENT '联系人电话',
    province        VARCHAR(50) NOT NULL COMMENT '省',
    city            VARCHAR(50) NOT NULL COMMENT '市',
    district        VARCHAR(50) COMMENT '区/县',
    detail_address  VARCHAR(500) NOT NULL COMMENT '详细地址',
    longitude       DECIMAL(10, 7) COMMENT '经度',
    latitude        DECIMAL(10, 7) COMMENT '纬度',
    is_default      TINYINT DEFAULT 0 COMMENT '是否默认：0-否 1-是',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_user_id (user_id),
    INDEX idx_city (city),
    INDEX idx_location (longitude, latitude)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户地址表';

-- ------------------------------------------------------------
-- 2. 商品/品类模块
-- ------------------------------------------------------------

-- 商品分类表
CREATE TABLE categories (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id       BIGINT DEFAULT 0 COMMENT '父分类ID，0为顶级',
    name            VARCHAR(100) NOT NULL COMMENT '分类名称',
    icon_url        VARCHAR(500) COMMENT '图标URL',
    sort_order      INT DEFAULT 0 COMMENT '排序',
    status          TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 供应商商品库（供应商维护的常供商品）
CREATE TABLE supplier_products (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    supplier_id         BIGINT NOT NULL COMMENT '供应商ID',
    category_id         BIGINT NOT NULL COMMENT '分类ID',
    name                VARCHAR(200) NOT NULL COMMENT '商品名称',
    spec                VARCHAR(100) COMMENT '规格（如：500g/袋）',
    origin              VARCHAR(100) COMMENT '产地',
    grade               VARCHAR(50) COMMENT '等级（特级/一级/二级）',
    main_image          VARCHAR(500) COMMENT '主图',
    description         TEXT COMMENT '商品描述',
    min_order_quantity  DECIMAL(10, 2) COMMENT '最小起订量',
    supply_cities       JSON COMMENT '供应城市列表 ["成都","重庆"]',
    status              TINYINT DEFAULT 1 COMMENT '状态：0-下架 1-上架',
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (supplier_id) REFERENCES users(id),
    FOREIGN KEY (category_id) REFERENCES categories(id),
    INDEX idx_supplier_id (supplier_id),
    INDEX idx_category_id (category_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商商品库';

-- ------------------------------------------------------------
-- 3. 需求与订单核心模块
-- ------------------------------------------------------------

-- 需求合并组（同城同类需求的聚合）
CREATE TABLE demand_groups (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id         BIGINT NOT NULL COMMENT '商品分类ID',
    product_name        VARCHAR(200) NOT NULL COMMENT '商品名称（冗余，方便查询）',
    city                VARCHAR(50) NOT NULL COMMENT '城市',
    total_quantity      DECIMAL(10, 2) NOT NULL COMMENT '合并总数量',
    unit                VARCHAR(20) COMMENT '单位（斤/公斤/件）',
    merge_deadline      TIMESTAMP COMMENT '合并截止时间',
    status              TINYINT DEFAULT 0 COMMENT '状态：0-待合并 1-合并中 2-报价中 3-已成交 4-已关闭',
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id),
    INDEX idx_city (city),
    INDEX idx_category_id (category_id),
    INDEX idx_status (status),
    INDEX idx_merge_deadline (merge_deadline)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='需求合并组';

-- 采购商需求（子订单）
CREATE TABLE demands (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id            BIGINT COMMENT '所属合并组ID，未合并时为NULL',
    buyer_id            BIGINT NOT NULL COMMENT '采购商ID',
    category_id         BIGINT NOT NULL COMMENT '商品分类ID',
    product_name        VARCHAR(200) NOT NULL COMMENT '商品名称',
    quantity            DECIMAL(10, 2) NOT NULL COMMENT '需求数量',
    unit                VARCHAR(20) COMMENT '单位',
    max_price           DECIMAL(10, 2) COMMENT '期望最高单价',
    quality_requirement VARCHAR(500) COMMENT '品质要求',
    delivery_address_id BIGINT NOT NULL COMMENT '收货地址ID',
    delivery_date       DATE COMMENT '期望交货日期',
    delivery_time_slot  VARCHAR(50) COMMENT '期望时间段（如：上午9-12点）',
    remark              VARCHAR(1000) COMMENT '备注',
    
    -- 订单状态流转
    status              TINYINT DEFAULT 0 COMMENT '状态：0-待发布 1-待合并 2-合并中 3-报价中 4-待选择 5-待发货 6-已发货 7-已签收 8-已完成 9-已取消',
    
    -- 成交信息
    selected_quote_id   BIGINT COMMENT '选中的报价ID',
    deal_price          DECIMAL(10, 2) COMMENT '成交单价',
    deal_total_amount   DECIMAL(12, 2) COMMENT '成交总价',
    supplier_id         BIGINT COMMENT '成交供应商ID',
    
    -- 打包发货相关
    pack_status         TINYINT DEFAULT 0 COMMENT '打包状态：0-待分拣 1-分拣中 2-已打包 3-已发货',
    actual_weight       DECIMAL(10, 2) COMMENT '实际称重',
    pack_remark         VARCHAR(500) COMMENT '打包备注',
    
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (group_id) REFERENCES demand_groups(id),
    FOREIGN KEY (buyer_id) REFERENCES users(id),
    FOREIGN KEY (category_id) REFERENCES categories(id),
    FOREIGN KEY (delivery_address_id) REFERENCES user_addresses(id),
    FOREIGN KEY (supplier_id) REFERENCES users(id),
    INDEX idx_group_id (group_id),
    INDEX idx_buyer_id (buyer_id),
    INDEX idx_supplier_id (supplier_id),
    INDEX idx_status (status),
    INDEX idx_pack_status (pack_status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购商需求（子订单）';

-- 供应商报价表
CREATE TABLE quotes (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id            BIGINT NOT NULL COMMENT '合并组ID',
    supplier_id         BIGINT NOT NULL COMMENT '供应商ID',
    unit_price          DECIMAL(10, 2) NOT NULL COMMENT '报价单价',
    total_amount        DECIMAL(12, 2) COMMENT '报价总价（单价×合并总量）',
    valid_hours         INT DEFAULT 24 COMMENT '报价有效期（小时）',
    expire_at           TIMESTAMP COMMENT '过期时间',
    remark              VARCHAR(500) COMMENT '报价说明（如：量大从优）',
    status              TINYINT DEFAULT 0 COMMENT '状态：0-待审核 1-有效 2-已过期 3-已撤销 4-已选中',
    selected_at         TIMESTAMP COMMENT '被选中时间',
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES demand_groups(id),
    FOREIGN KEY (supplier_id) REFERENCES users(id),
    INDEX idx_group_id (group_id),
    INDEX idx_supplier_id (supplier_id),
    INDEX idx_status (status),
    INDEX idx_expire_at (expire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商报价表';

-- ------------------------------------------------------------
-- 4. 分拣打包模块（供应商端核心）
-- ------------------------------------------------------------

-- 打包记录表
CREATE TABLE pack_records (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    demand_id           BIGINT NOT NULL COMMENT '子订单ID',
    supplier_id         BIGINT NOT NULL COMMENT '供应商ID',
    operator_id         BIGINT COMMENT '分拣员ID（如果是员工操作）',
    
    -- 分拣信息
    planned_quantity    DECIMAL(10, 2) NOT NULL COMMENT '计划数量（订单数量）',
    actual_quantity     DECIMAL(10, 2) COMMENT '实际分拣数量',
    actual_weight       DECIMAL(10, 2) COMMENT '实际称重（kg）',
    weight_deviation    DECIMAL(5, 2) COMMENT '重量偏差百分比',
    
    -- 品质信息
    grade               VARCHAR(50) COMMENT '实际等级',
    quality_check       TINYINT DEFAULT 1 COMMENT '质检结果：0-不合格 1-合格',
    
    -- 包装信息
    package_count       INT COMMENT '包裹数量',
    package_type        VARCHAR(50) COMMENT '包装类型（泡沫箱/纸箱/袋装）',
    label_code          VARCHAR(100) COMMENT '标签码（二维码/条形码）',
    
    -- 照片存档
    photos              JSON COMMENT '打包照片URL列表',
    
    -- 状态
    status              TINYINT DEFAULT 0 COMMENT '状态：0-待分拣 1-分拣中 2-已打包 3-已发货',
    
    packed_at           TIMESTAMP COMMENT '打包完成时间',
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (demand_id) REFERENCES demands(id),
    FOREIGN KEY (supplier_id) REFERENCES users(id),
    INDEX idx_demand_id (demand_id),
    INDEX idx_supplier_id (supplier_id),
    INDEX idx_label_code (label_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打包记录表';

-- 包裹明细表（一个子订单可能有多个包裹）
CREATE TABLE packages (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    pack_record_id      BIGINT NOT NULL COMMENT '打包记录ID',
    demand_id           BIGINT NOT NULL COMMENT '子订单ID',
    
    -- 包裹信息
    package_no          VARCHAR(50) NOT NULL COMMENT '包裹编号（唯一）',
    weight              DECIMAL(10, 2) COMMENT '包裹重量',
    items_count         INT COMMENT '内含件数',
    label_code          VARCHAR(100) COMMENT '标签码',
    
    -- 状态
    status              TINYINT DEFAULT 0 COMMENT '状态：0-待发货 1-已发货 2-运输中 3-已签收',
    
    -- 物流信息
    logistics_company   VARCHAR(100) COMMENT '物流公司',
    tracking_no         VARCHAR(100) COMMENT '物流单号',
    shipped_at          TIMESTAMP COMMENT '发货时间',
    delivered_at        TIMESTAMP COMMENT '签收时间',
    
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (pack_record_id) REFERENCES pack_records(id),
    FOREIGN KEY (demand_id) REFERENCES demands(id),
    UNIQUE KEY uk_package_no (package_no),
    INDEX idx_pack_record_id (pack_record_id),
    INDEX idx_demand_id (demand_id),
    INDEX idx_tracking_no (tracking_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='包裹明细表';

-- ------------------------------------------------------------
-- 5. 配送与物流模块
-- ------------------------------------------------------------

-- 配送路线表（同城多订单合并配送）
CREATE TABLE delivery_routes (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    supplier_id         BIGINT NOT NULL COMMENT '供应商ID',
    route_date          DATE NOT NULL COMMENT '配送日期',
    
    -- 路线信息
    start_address_id    BIGINT COMMENT '起始地址ID（仓库）',
    stop_count          INT DEFAULT 0 COMMENT '配送点数量',
    total_distance      DECIMAL(8, 2) COMMENT '总距离（公里）',
    estimated_duration  INT COMMENT '预计时长（分钟）',
    
    -- 路线优化结果（存储经纬度序列）
    route_points        JSON COMMENT '路线点 [{"address_id":1,"lng":104,"lat":30,"seq":1}]',
    
    -- 状态
    status              TINYINT DEFAULT 0 COMMENT '状态：0-规划中 1-已确认 2-配送中 3-已完成',
    
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (supplier_id) REFERENCES users(id),
    INDEX idx_supplier_id (supplier_id),
    INDEX idx_route_date (route_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配送路线表';

-- 路线与订单关联表
CREATE TABLE route_demands (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    route_id            BIGINT NOT NULL COMMENT '路线ID',
    demand_id           BIGINT NOT NULL COMMENT '子订单ID',
    sequence            INT NOT NULL COMMENT '配送顺序',
    estimated_arrival   TIME COMMENT '预计到达时间',
    
    FOREIGN KEY (route_id) REFERENCES delivery_routes(id),
    FOREIGN KEY (demand_id) REFERENCES demands(id),
    UNIQUE KEY uk_route_demand (route_id, demand_id),
    INDEX idx_route_id (route_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='路线订单关联表';

-- ------------------------------------------------------------
-- 6. 评价与售后模块
-- ------------------------------------------------------------

-- 评价表
CREATE TABLE reviews (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    demand_id           BIGINT NOT NULL COMMENT '子订单ID',
    reviewer_id         BIGINT NOT NULL COMMENT '评价人ID',
    target_id           BIGINT NOT NULL COMMENT '被评价人ID',
    review_type         TINYINT NOT NULL COMMENT '评价类型：1-采购商评供应商 2-供应商评采购商',
    
    -- 评分
    quality_score       TINYINT COMMENT '品质评分 1-5',
    service_score       TINYINT COMMENT '服务评分 1-5',
    delivery_score      TINYINT COMMENT '配送评分 1-5',
    overall_score       TINYINT COMMENT '综合评分 1-5',
    
    -- 评价内容
    content             VARCHAR(2000) COMMENT '评价内容',
    images              JSON COMMENT '评价图片',
    
    -- 回复
    reply_content       VARCHAR(1000) COMMENT '商家回复',
    replied_at          TIMESTAMP COMMENT '回复时间',
    
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (demand_id) REFERENCES demands(id),
    FOREIGN KEY (reviewer_id) REFERENCES users(id),
    FOREIGN KEY (target_id) REFERENCES users(id),
    INDEX idx_demand_id (demand_id),
    INDEX idx_target_id (target_id),
    INDEX idx_review_type (review_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- ------------------------------------------------------------
-- 7. 消息通知模块
-- ------------------------------------------------------------

-- 消息表
CREATE TABLE messages (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id             BIGINT NOT NULL COMMENT '接收用户ID',
    msg_type            TINYINT NOT NULL COMMENT '消息类型：1-系统 2-订单 3-报价 4-物流',
    title               VARCHAR(200) NOT NULL COMMENT '标题',
    content             TEXT COMMENT '内容',
    biz_type            VARCHAR(50) COMMENT '业务类型（demand/quote/order等）',
    biz_id              BIGINT COMMENT '业务ID',
    extras              JSON COMMENT '扩展信息',
    
    is_read             TINYINT DEFAULT 0 COMMENT '是否已读：0-未读 1-已读',
    read_at             TIMESTAMP COMMENT '阅读时间',
    
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_user_id (user_id),
    INDEX idx_msg_type (msg_type),
    INDEX idx_is_read (is_read),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- ------------------------------------------------------------
-- 8. 初始化数据
-- ------------------------------------------------------------

-- 初始化商品分类
INSERT INTO categories (id, parent_id, name, sort_order, status) VALUES
(1, 0, '蔬菜', 1, 1),
(2, 0, '水果', 2, 1),
(3, 0, '肉禽蛋', 3, 1),
(4, 0, '水产海鲜', 4, 1),
(5, 0, '粮油干货', 5, 1),
(6, 1, '叶菜类', 1, 1),
(7, 1, '根茎类', 2, 1),
(8, 1, '茄果类', 3, 1),
(9, 2, '柑橘类', 1, 1),
(10, 2, '浆果类', 2, 1);
