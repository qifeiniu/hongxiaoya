CREATE TABLE `activities` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '活动名称',
  `cover_img` varchar(255) DEFAULT NULL COMMENT '封面图',
  `description` text COMMENT '简介',
  `requirements` text COMMENT '报名要求',
  `creator_id` bigint unsigned NOT NULL COMMENT '创建者红娘ID',
  `male_quota` int NOT NULL DEFAULT '0' COMMENT '男名额',
  `female_quota` int NOT NULL DEFAULT '0' COMMENT '女名额',
  `price` int NOT NULL DEFAULT '0' COMMENT '报名费',
  `start_time` timestamp NULL DEFAULT NULL COMMENT '开始时间',
  `duration` int DEFAULT '60' COMMENT '时长(分钟)',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-报名中, 1-进行中, 2-已结束',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='相亲活动表';
