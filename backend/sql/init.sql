-- ==========================================================
-- 红小鸭相亲交友APP 数据库核心表结构 (v1.0)
-- 适用数据库: MySQL 8.0+
-- 字符集: utf8mb4, 排序规则: utf8mb4_unicode_ci
-- ==========================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------------------------------------
-- 1. 用户核心表 (Users)
-- ----------------------------------------------------------
CREATE TABLE `users` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `wx_unionid` varchar(100) DEFAULT NULL COMMENT '微信UnionID',
  `wx_openid` varchar(100) DEFAULT NULL COMMENT '微信OpenID',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 1-正常, 2-封禁, 3-注销',
  `role` tinyint NOT NULL DEFAULT '1' COMMENT '角色: 1-普通用户, 2-红娘',
  `referrer_id` bigint unsigned DEFAULT NULL COMMENT '推荐人用户ID',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_wx_unionid` (`wx_unionid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户账号表';

-- ----------------------------------------------------------
-- 2. 用户资料表 (User Profiles)
-- ----------------------------------------------------------
CREATE TABLE `user_profiles` (
  `user_id` bigint unsigned NOT NULL COMMENT '用户ID',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `gender` tinyint DEFAULT '0' COMMENT '性别: 1-男, 2-女, 0-未知',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `height` int DEFAULT NULL COMMENT '身高(cm)',
  `weight` int DEFAULT NULL COMMENT '体重(kg)',
  `city_code` varchar(20) DEFAULT NULL COMMENT '居住地城市编码',
  `hometown_code` varchar(20) DEFAULT NULL COMMENT '籍贯编码',
  `household_code` varchar(20) DEFAULT NULL COMMENT '户口所在地编码',
  `education` tinyint DEFAULT '0' COMMENT '学历: 1-专科, 2-本科, 3-硕士, 4-博士',
  `school` varchar(100) DEFAULT NULL COMMENT '毕业院校',
  `income_level` tinyint DEFAULT '0' COMMENT '年收入范围枚举',
  `profession` varchar(50) DEFAULT NULL COMMENT '职业',
  `marital_status` tinyint DEFAULT '1' COMMENT '婚姻状态: 1-未婚, 2-离异, 3-丧偶',
  `constellation` varchar(20) DEFAULT NULL COMMENT '星座',
  `bio` text COMMENT '个人介绍/爱情宣言',
  `family_background` varchar(500) DEFAULT NULL COMMENT '家庭背景',
  `hobbies` varchar(500) DEFAULT NULL COMMENT '兴趣爱好',
  `ideal_partner` varchar(500) DEFAULT NULL COMMENT '理想另一半',
  `expected_life` varchar(500) DEFAULT NULL COMMENT '期待生活',
  `photos` json DEFAULT NULL COMMENT '个人相册(JSON数组)',
  `tags` json DEFAULT NULL COMMENT '个人标签(JSON数组)',
  `is_real_auth` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否实名认证: 0-否, 1-是',
  `is_edu_auth` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否学历认证: 0-否, 1-是',
  `is_avatar_auth` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否头像认证: 0-否, 1-是',
  `wechat_id` varchar(50) DEFAULT NULL COMMENT '个人微信号(付费解锁用)',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户资料表';

-- ----------------------------------------------------------
-- 3. 会员权益表 (VIP Memberships)
-- ----------------------------------------------------------
CREATE TABLE `vip_memberships` (
  `user_id` bigint unsigned NOT NULL COMMENT '用户ID',
  `level` tinyint NOT NULL DEFAULT '1' COMMENT '会员等级: 1-普通会员',
  `expire_at` timestamp NULL DEFAULT NULL COMMENT '会员到期时间',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  KEY `idx_expire_at` (`expire_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员权益表';

-- ----------------------------------------------------------
-- 4. 鸭蛋钱包表 (Wallets)
-- ----------------------------------------------------------
CREATE TABLE `wallets` (
  `user_id` bigint unsigned NOT NULL COMMENT '用户ID',
  `balance` int NOT NULL DEFAULT '0' COMMENT '总鸭蛋余额',
  `permanent_balance` int NOT NULL DEFAULT '0' COMMENT '永久有效鸭蛋余额(充值获得)',
  `temp_balance` int NOT NULL DEFAULT '0' COMMENT '限期有效鸭蛋余额(签到/赠送等获得)',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户钱包表';

-- ----------------------------------------------------------
-- 5. 鸭蛋收支流水表 (Egg Ledgers)
-- ----------------------------------------------------------
CREATE TABLE `egg_ledgers` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned NOT NULL COMMENT '用户ID',
  `amount` int NOT NULL COMMENT '变动数量(正数增加，负数减少)',
  `balance_after` int NOT NULL COMMENT '变动后总余额',
  `scene` varchar(30) NOT NULL COMMENT '场景枚举: register, sign_in, recharge, send_gift, unlock_wechat, video_call...',
  `related_id` varchar(64) DEFAULT NULL COMMENT '关联业务单号(如订单号/通话记录ID)',
  `target_user_id` bigint unsigned DEFAULT NULL COMMENT '交互目标用户ID(如送礼对象)',
  `expire_at` timestamp NULL DEFAULT NULL COMMENT '过期时间(若为增加的限期鸭蛋)',
  `idempotent_key` varchar(64) DEFAULT NULL COMMENT '幂等防重键',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id_scene` (`user_id`, `scene`),
  UNIQUE KEY `uk_idempotent` (`idempotent_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='鸭蛋收支流水表';

-- ----------------------------------------------------------
-- 6. 用户交互关系表 (User Relations)
-- ----------------------------------------------------------
CREATE TABLE `user_relations` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned NOT NULL COMMENT '发起方用户ID',
  `target_id` bigint unsigned NOT NULL COMMENT '目标方用户ID',
  `relation_type` tinyint NOT NULL COMMENT '关系类型: 1-喜欢(红心), 2-不喜欢, 3-黑名单(屏蔽)',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`, `target_id`),
  KEY `idx_target_type` (`target_id`, `relation_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户互动关系表(喜欢/不喜欢/屏蔽)';

-- ----------------------------------------------------------
-- 7. 付费解锁记录表 (Unlock Records)
-- ----------------------------------------------------------
CREATE TABLE `unlock_records` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned NOT NULL COMMENT '解锁方用户ID',
  `target_id` bigint unsigned NOT NULL COMMENT '被解锁方用户ID',
  `unlock_type` tinyint NOT NULL COMMENT '解锁类型: 1-微信号, 2-访客记录, 3-谁喜欢我',
  `cost_eggs` int NOT NULL COMMENT '消耗鸭蛋数',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target_type` (`user_id`, `target_id`, `unlock_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='付费解锁持久化状态表';

-- ----------------------------------------------------------
-- 8. 访客记录表 (Visitor Records)
-- ----------------------------------------------------------
CREATE TABLE `visitor_records` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned NOT NULL COMMENT '被访问用户ID',
  `visitor_id` bigint unsigned NOT NULL COMMENT '访客用户ID',
  `visit_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`, `visit_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='访客记录表';

-- ----------------------------------------------------------
-- 9. 聊天消息表 (Messages)
-- ----------------------------------------------------------
CREATE TABLE `messages` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `sender_id` bigint unsigned NOT NULL COMMENT '发送方用户ID',
  `receiver_id` bigint unsigned NOT NULL COMMENT '接收方用户ID',
  `msg_type` tinyint NOT NULL DEFAULT '1' COMMENT '消息类型: 1-文字, 2-图片, 3-语音, 4-视频, 5-礼物, 6-系统',
  `content` text COMMENT '消息内容/图片URL/语音URL',
  `is_read` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已读: 0-未读, 1-已读',
  `is_recalled` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已撤回: 0-否, 1-是',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`),
  KEY `idx_sender_receiver` (`sender_id`, `receiver_id`),
  KEY `idx_receiver_read` (`receiver_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

-- ----------------------------------------------------------
-- 10. 会话列表表 (Conversations)
-- ----------------------------------------------------------
CREATE TABLE `conversations` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned NOT NULL COMMENT '所属用户ID',
  `target_id` bigint unsigned NOT NULL COMMENT '聊天对象ID',
  `last_msg_id` bigint unsigned DEFAULT NULL COMMENT '最后一条消息ID',
  `unread_count` int NOT NULL DEFAULT '0' COMMENT '未读消息数',
  `is_top` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否置顶: 0-否, 1-是',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`, `target_id`),
  KEY `idx_user_updated` (`user_id`, `updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话列表表';

-- ----------------------------------------------------------
-- 11. 漂流瓶表 (Bottles)
-- ----------------------------------------------------------
CREATE TABLE `bottles` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned NOT NULL COMMENT '扔瓶子用户ID',
  `content` text COMMENT '漂流瓶内容',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-未捞起, 1-已捞起',
  `picked_by` bigint unsigned DEFAULT NULL COMMENT '捞起者用户ID',
  `picked_at` timestamp NULL DEFAULT NULL COMMENT '捞起时间',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '扔出时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='漂流瓶表';

SET FOREIGN_KEY_CHECKS = 1;
