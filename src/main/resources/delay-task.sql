CREATE TABLE `delay_task_clock` (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `task_trigger_time` bigint(19) NOT NULL COMMENT '执行时间',
  `clock_status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '任务状态：1.未处理 2.时钟已加载 3.执行 4.过期不再执行 5.过期执行',
  `gmt_create` bigint(20) NOT NULL COMMENT '创建时间',
  `gmt_update` bigint(20) NOT NULL COMMENT '最近一次更新时间',
  `last_ver` int(10) NOT NULL DEFAULT '0' COMMENT '版本号',
  `is_valid` tinyint(2) NOT NULL DEFAULT '1' COMMENT '是否有效 0-失效 1-有效',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_idx_ttime` (`task_trigger_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='延迟任务调度时钟表';

CREATE TABLE `delay_task_info` (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `app_name` varchar(32) NOT NULL DEFAULT '' COMMENT '业务方应用名称',
  `sharding_id` TINYINT (4) NOT NULL COMMENT '分片ID',
  `topic` varchar(100) NOT NULL DEFAULT '' COMMENT '消息topic',
  `producer_group_id` varchar(100) NOT NULL DEFAULT '' COMMENT '生产者组ID',
  `tag` varchar(100) NOT NULL DEFAULT '' COMMENT '消息tag',
  `params` varchar(1000) NOT NULL DEFAULT '' COMMENT '参数',
  `task_trigger_time` bigint(20) NOT NULL COMMENT '执行时间',
  `task_status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '任务状态：1.初始化 2.消息已发放 3.业务处理成功 4.业务处理失败 5.任务取消',
  `msg_id` varchar(32) NOT NULL DEFAULT '' COMMENT '消息ID',
  `extend_field` varchar(100) NOT NULL DEFAULT '' COMMENT '扩展属性',
  `gmt_create` bigint(20) NOT NULL COMMENT '创建时间',
  `gmt_update` bigint(20) NOT NULL COMMENT '最近一次更新时间',
  `last_ver` int(10) NOT NULL DEFAULT '0' COMMENT '版本号',
  `is_valid` tinyint(2) NOT NULL DEFAULT '1' COMMENT '是否有效 0-失效 1-有效',
  PRIMARY KEY (`id`),
  KEY `idx_trigger_time_tstatus` (`task_trigger_time`,`task_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='延迟任务信息表';

