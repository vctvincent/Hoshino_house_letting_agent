-- =====================================================
-- 房屋中介管理系统数据库设计 (完整版)
-- 创建时间：2026-02-27
-- 数据库版本：MySQL 8.0+
-- 修正说明：统一索引名与字段名，修复外键和插入语句
-- 更新说明：添加消息通知表 h_message
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS reams
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE reams;

-- 先关闭外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 第 1 层：基础表（无外部依赖）
-- =====================================================

-- 1.1 管理员表
DROP TABLE IF EXISTS sys_admin;
CREATE TABLE sys_admin (
    admin_id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '管理员 ID',
    admin_name            VARCHAR(50) NOT NULL DEFAULT 'admin' COMMENT '管理员名称',
    admin_password        VARCHAR(200) NOT NULL COMMENT '密码 (BCrypt 加密)',
    admin_email           VARCHAR(100) COMMENT '邮箱',
    admin_phone           VARCHAR(20) COMMENT '联系电话',
    admin_avatar          VARCHAR(500) COMMENT '头像 URL',
    admin_create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    admin_update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_admin_name (admin_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 1.2 中介表
DROP TABLE IF EXISTS sys_agent;
CREATE TABLE sys_agent (
    agent_id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '中介 ID',
    agent_password        VARCHAR(200) NOT NULL COMMENT '密码 (BCrypt 加密)',
    agent_name            VARCHAR(50) NOT NULL COMMENT '姓名',
    agent_phone           VARCHAR(20) NOT NULL COMMENT '手机号 (AES 加密)',
    agent_email           VARCHAR(100) COMMENT '邮箱',
    agent_avatar          VARCHAR(500) COMMENT '头像 URL',
    agent_gender          TINYINT COMMENT '性别：0-未知 1-男 2-女',
    agent_rating          DECIMAL(3,2) DEFAULT 5.00 COMMENT '评分 (0-5)',
    agent_deal_count      INT DEFAULT 0 COMMENT '成交数量',
    agent_years_experience INT DEFAULT 0 COMMENT '从业年限',
    agent_introduction    TEXT COMMENT '个人简介',
    agent_company         VARCHAR(100) COMMENT '所属公司',
    agent_status          TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-正常 2-待审核',
    agent_create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    agent_update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_agent_phone (agent_phone),
    INDEX idx_agent_status (agent_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='中介表';

-- 1.3 客户表
DROP TABLE IF EXISTS sys_customer;
CREATE TABLE sys_customer (
    customer_id           BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '客户 ID',
    customer_password     VARCHAR(200) NOT NULL COMMENT '密码 (BCrypt 加密)',
    customer_nickname     VARCHAR(50) COMMENT '昵称',
    customer_phone        VARCHAR(20) NOT NULL COMMENT '手机号 (AES 加密)',
    customer_email        VARCHAR(100) COMMENT '邮箱',
    customer_gender       TINYINT COMMENT '性别：0-未知 1-男 2-女',
    customer_avatar       VARCHAR(500) COMMENT '头像 URL',
    customer_status       TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
    customer_create_time  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    customer_update_time  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_customer_phone (customer_phone),
    INDEX idx_customer_status (customer_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户表';

-- =====================================================
-- 第 2 层：核心业务表
-- =====================================================

-- 2.1 房源表
DROP TABLE IF EXISTS h_house;
CREATE TABLE h_house (
    house_id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '房源 ID',
    agent_id              BIGINT NOT NULL COMMENT '中介 ID',
    house_title           VARCHAR(200) NOT NULL COMMENT '房源标题',
    house_address         VARCHAR(500) NOT NULL COMMENT '详细地址',
    house_province        VARCHAR(50) COMMENT '省份',
    house_city            VARCHAR(50) COMMENT '城市',
    house_district        VARCHAR(50) COMMENT '区县',
    house_community       VARCHAR(100) COMMENT '小区名称',
    house_area            DECIMAL(10,2) COMMENT '面积 (平方米)',
    house_price           DECIMAL(15,2) COMMENT '售价 (万元)',
    house_unit_price      DECIMAL(10,2) COMMENT '单价 (元/平米)',
    house_type            VARCHAR(20) COMMENT '户型',
    house_layout          VARCHAR(50) COMMENT '户型结构',
    house_floor           VARCHAR(20) COMMENT '楼层',
    house_total_floor     INT COMMENT '总楼层',
    house_building_year   INT COMMENT '建筑年代',
    house_orientation     VARCHAR(20) COMMENT '朝向',
    house_decoration      VARCHAR(20) COMMENT '装修情况',
    house_property_type   VARCHAR(20) COMMENT '房屋类型',
    house_property_fee    DECIMAL(10,2) COMMENT '物业费',
    house_elevator        TINYINT COMMENT '是否有电梯',
    house_heating         TINYINT COMMENT '供暖',
    house_description     TEXT COMMENT '房源描述',
    house_images          JSON COMMENT '房源图片 (JSON 数组)',
    house_video_url       VARCHAR(500) COMMENT '房源视频 URL',
    house_facilities      JSON COMMENT '配套设施 (JSON 数组)',
    house_tags            VARCHAR(500) COMMENT '标签',
    house_status          TINYINT DEFAULT 0 COMMENT '房源状态：0-未发布 1-已发布 2-已成交 3-已下架',
    house_view_count      INT DEFAULT 0 COMMENT '浏览次数',
    house_favorite_count  INT DEFAULT 0 COMMENT '收藏次数',
    house_audit_status    TINYINT DEFAULT 0 COMMENT '审核状态：0-待审核 1-审核中 2-通过 3-拒绝',
    house_reject_reason   VARCHAR(500) COMMENT '拒绝原因',
    house_create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    house_update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_agent_id (agent_id),
    INDEX idx_house_address (house_address),
    INDEX idx_house_price (house_price),
    INDEX idx_house_district (house_district),
    INDEX idx_house_status (house_status),
    INDEX idx_house_audit_status (house_audit_status),
    INDEX idx_house_create_time (house_create_time),
    FOREIGN KEY (agent_id) REFERENCES sys_agent(agent_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房源表';

-- 2.2 带看记录表
DROP TABLE IF EXISTS h_viewing;
CREATE TABLE h_viewing (
    viewing_id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '带看 ID',
    customer_id           BIGINT NOT NULL COMMENT '客户 ID',
    agent_id              BIGINT NOT NULL COMMENT '中介 ID',
    house_id              BIGINT NOT NULL COMMENT '房源 ID',
    viewing_appoint_time  DATETIME NOT NULL COMMENT '预约时间',
    viewing_actual_time   DATETIME COMMENT '实际看房时间',
    viewing_address       VARCHAR(500) COMMENT '看房地址',
    viewing_status        TINYINT DEFAULT 0 COMMENT '状态：0-待确认 1-已确认 2-已完成 3-已取消',
    viewing_cancel_reason VARCHAR(500) COMMENT '取消原因',
    viewing_cancel_by_type TINYINT COMMENT '取消操作人类型：1-管理员 2-中介 3-客户',
    viewing_cancel_by_id  BIGINT COMMENT '取消操作人 ID',
    viewing_remark        VARCHAR(500) COMMENT '备注',
    customer_phone        VARCHAR(20) COMMENT '客户电话 (快照)',
    viewing_create_time   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    viewing_update_time   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_customer_id (customer_id),
    INDEX idx_agent_id (agent_id),
    INDEX idx_house_id (house_id),
    INDEX idx_viewing_status (viewing_status),
    INDEX idx_viewing_appoint_time (viewing_appoint_time),
    FOREIGN KEY (customer_id) REFERENCES sys_customer(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (agent_id) REFERENCES sys_agent(agent_id) ON DELETE CASCADE,
    FOREIGN KEY (house_id) REFERENCES h_house(house_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='带看记录表';

-- 2.3 交易表 (h_transaction)
-- =====================================================
DROP TABLE IF EXISTS h_transaction;
CREATE TABLE h_transaction (
    transaction_id        BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '交易 ID',
    house_id              BIGINT NOT NULL COMMENT '房源 ID',
    customer_id           BIGINT NOT NULL COMMENT '客户 ID',
    agent_id              BIGINT NOT NULL COMMENT '中介 ID',
    viewing_id            BIGINT COMMENT '带看 ID',
    transaction_no        VARCHAR(50) UNIQUE COMMENT '交易单号',
    transaction_final_price           DECIMAL(15,2) NOT NULL COMMENT '成交价格 (万元)',
    transaction_deposit               DECIMAL(15,2) DEFAULT 0 COMMENT '定金 (万元)',
    transaction_payment_method        VARCHAR(20) COMMENT '付款方式：全款/分期/贷款',
    transaction_contract_url          VARCHAR(500) COMMENT '合同文件 URL',
    transaction_deal_date             DATE COMMENT '成交日期',
    transaction_status                TINYINT DEFAULT 0 COMMENT '状态：0-待确认 1-谈判中 2-已签约 3-已完成 4-已取消',
    transaction_status_history        TEXT COMMENT '状态变更历史 (JSON 格式)',
    transaction_remark                VARCHAR(1000) COMMENT '备注/操作日志',
    transaction_create_time           DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    transaction_update_time           DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_house_id (house_id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_agent_id (agent_id),
    INDEX idx_transaction_no (transaction_no),
    INDEX idx_status (transaction_status),
    FOREIGN KEY (house_id) REFERENCES h_house(house_id) ON DELETE CASCADE,
    FOREIGN KEY (customer_id) REFERENCES sys_customer(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (agent_id) REFERENCES sys_agent(agent_id) ON DELETE CASCADE,
    FOREIGN KEY (viewing_id) REFERENCES h_viewing(viewing_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易表';

-- 2.4 收藏表
DROP TABLE IF EXISTS h_favorite;
CREATE TABLE h_favorite (
    favorite_id           BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '收藏 ID',
    customer_id           BIGINT NOT NULL COMMENT '客户 ID',
    house_id              BIGINT NOT NULL COMMENT '房源 ID',
    favorite_create_time  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_customer_house (customer_id, house_id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_house_id (house_id),
    FOREIGN KEY (customer_id) REFERENCES sys_customer(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (house_id) REFERENCES h_house(house_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- 2.5 评价表
DROP TABLE IF EXISTS h_review;
CREATE TABLE h_review (
    review_id             BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评价 ID',
    transaction_id        BIGINT COMMENT '交易 ID',
    viewing_id            BIGINT COMMENT '带看 ID',
    house_id              BIGINT COMMENT '房源 ID',
    agent_id              BIGINT COMMENT '中介 ID',
    customer_id           BIGINT NOT NULL COMMENT '客户 ID',
    review_target_type    TINYINT NOT NULL COMMENT '评价对象：1-房源 2-中介 3-整体',
    review_rating         TINYINT NOT NULL COMMENT '评分 (1-5)',
    review_content        TEXT COMMENT '评价内容',
    review_images         JSON COMMENT '评价图片 (JSON 数组)',
    review_reply_content  VARCHAR(500) COMMENT '回复内容',
    review_reply_time     DATETIME COMMENT '回复时间',
    review_is_show        TINYINT DEFAULT 1 COMMENT '是否显示：0-隐藏 1-显示',
    review_create_time    DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    review_update_time    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_viewing_id (viewing_id),
    INDEX idx_house_id (house_id),
    INDEX idx_agent_id (agent_id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_review_target_type (review_target_type),
    FOREIGN KEY (transaction_id) REFERENCES h_transaction(transaction_id) ON DELETE SET NULL,
    FOREIGN KEY (viewing_id) REFERENCES h_viewing(viewing_id) ON DELETE SET NULL,
    FOREIGN KEY (house_id) REFERENCES h_house(house_id) ON DELETE SET NULL,
    FOREIGN KEY (agent_id) REFERENCES sys_agent(agent_id) ON DELETE SET NULL,
    FOREIGN KEY (customer_id) REFERENCES sys_customer(customer_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- =====================================================
-- 第 3 层：扩展功能表
-- =====================================================

-- 3.1 消息通知表
DROP TABLE IF EXISTS h_message;
CREATE TABLE h_message (
    message_id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息 ID',
    message_sender_id             BIGINT NOT NULL COMMENT '发送者 ID',
    message_sender_type           TINYINT NOT NULL COMMENT '发送者类型：1-管理员 2-中介 3-客户',
    message_receiver_id           BIGINT NOT NULL COMMENT '接收者 ID',
    message_receiver_type         TINYINT NOT NULL COMMENT '接收者类型：1-管理员 2-中介 3-客户',
    message_type                  TINYINT NOT NULL DEFAULT 2 COMMENT '消息类型：1-系统通知 2-用户消息',
    message_content_type          VARCHAR(20) NOT NULL DEFAULT 'TEXT' COMMENT '内容类型：TEXT-文本 IMAGE-图片',
    message_scene                 VARCHAR(50) NOT NULL DEFAULT 'CHAT' COMMENT '消息场景：CHAT/VIEWING_REQUEST/VIEWING_REVIEW_INVITE/TRANSACTION_CREATED 等',
    message_title                 VARCHAR(200) NOT NULL COMMENT '消息标题',
    message_content               TEXT NOT NULL COMMENT '消息内容',
    message_house_id              BIGINT COMMENT '关联房源 ID',
    message_viewing_id            BIGINT COMMENT '关联带看 ID',
    message_is_read               TINYINT DEFAULT 0 COMMENT '是否已读：0-未读 1-已读',
    message_read_time             DATETIME COMMENT '阅读时间',
    message_create_time           DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    message_update_time           DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_sender (message_sender_id, message_sender_type),
    INDEX idx_receiver (message_receiver_id, message_receiver_type),
    INDEX idx_message_scene (message_scene),
    INDEX idx_message_house_id (message_house_id),
    INDEX idx_message_viewing_id (message_viewing_id),
    INDEX idx_is_read (message_is_read),
    INDEX idx_create_time (message_create_time),
    FOREIGN KEY (message_house_id) REFERENCES h_house(house_id) ON DELETE SET NULL,
    FOREIGN KEY (message_viewing_id) REFERENCES h_viewing(viewing_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';

-- 3.2 房源审核表
DROP TABLE IF EXISTS h_audit;
CREATE TABLE h_audit (
    audit_id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '审核 ID',
    house_id              BIGINT NOT NULL COMMENT '房源 ID',
    admin_id              BIGINT NOT NULL COMMENT '审核人 ID',
    audit_type            TINYINT DEFAULT 1 COMMENT '审核类型',
    audit_result          TINYINT NOT NULL COMMENT '审核结果：2-通过 3-拒绝',
    audit_reason          VARCHAR(500) COMMENT '审核意见',
    audit_time            DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
    audit_create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_house_id (house_id),
    INDEX idx_admin_id (admin_id),
    INDEX idx_audit_time (audit_time),
    FOREIGN KEY (house_id) REFERENCES h_house(house_id) ON DELETE CASCADE,
    FOREIGN KEY (admin_id) REFERENCES sys_admin(admin_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房源审核表';

-- 重新开启外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 初始化数据
-- =====================================================

-- 插入管理员 (密码：123456)
INSERT INTO sys_admin (admin_name, admin_password, admin_email, admin_phone) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'admin@reams.com', '13800138000');

SELECT '数据库初始化完成！共创建 10 张表：sys_admin, sys_agent, sys_customer, h_house, h_viewing, h_transaction, h_favorite, h_review, h_message, h_audit' AS result;
