# delay-task：延迟任务调度系统
## 一、延迟任务需求
### 1.1 需求描述
笔者接触过一些营销业务场景，比如说：
- 用户注册未登录过APP第二天早上10点发一条营销短信促活
- 红包过期前两天短信通知，下午16:00发送
- 等等定时任务处理业务。

针对以上场景采用的技术方案是定时任务扫数据汇总表，分页读取一定数量然后处理<br>
然而随着业务的发展，业务多元化，遇到了以下场景：
- 拼团砍价活动过期前半小时提醒
- 订单提交半小时内没有完成支付，订单自动取消，库存退还
- 用户2天内没有任何操作，发放激活短信

以上场景处理不是固定时间点触发，而是根据业务发生的时间推迟一段时间，针对以上的业务场景，我们考虑可以根据不同业务建表，然后每隔一段时间去定时扫表，各自处理业务。
但是随着业务增加，表泛滥，而且此类业务其实有很多相同的地方，那么我们可以考虑把相同逻辑抽离出来，构建一个底层服务，来实现延迟任务调度。

### 1.2 设计目标
1. 可靠性：任务进入调度系统之后，必须被执行一次
2. 高可用：支持多实例部署
3. 实时性：允许一定时间误差，当然误差越小越好
4. 可管理：支持消息删除
5. 高性能：数据量大的情况下也能保证高性能
6. 可扩展：增加和减少节点时，任务会重新分配

### 1.3 实现难点
1. 延迟任务如何调度
2. 如何满足服务的可靠性和任务调度的实时性


## 二、技术方案调研
- java.util.Timer + java.util.TimerTask
- java.util.concurrent.ScheduledExecutorService
- java.util.concurrent.DelayQueue
- 数据库轮询
- 时间轮
- redis过期键通知
- Quartz
- rocketMQ中的延迟消息

## 三、系统架构
延迟任务调度系统提供统一的任务操作接口给业务方调用，业务方可以提交任务，取消任务，查询任务状态。
调度服务属于底层应用，因此采用MQ的方式解耦，所有触发的延迟任务都通过消息的方式发送给业务消费方，
由消费方控制流量，业务幂等。同时也保证了任务的重试机制。

### 3.1 整体架构和流程设计|xxl-job + db + HashedWheelTimer + mq

![整体架构](/Users/caisl/work/program-project/github/delay-task/src/document/image/延迟任务调度服务各端交互架构图.jpg)

模块介绍：
**业务调用方**
- 业务方调用延迟任务服务添加/取消/修改延迟任务
- 到点触发的任务会放到MQ消息队列里面，由业务方自行消费
- 业务方消费消息处理完成之后，调用延迟任务服务通知处理结果

**延迟任务服务**
- 以RPC方式提供统一标准延迟任务接口给业务方操作，用于添加延迟任务，取消任务，反馈任务处理结果。
- 集成elastic-job提供数据分片功能，每个节点按照对应分片从数据库加载即将触发的延迟任务放到内存中
- 任务调度触发的延迟任务发送消息到MQ消息队列中
- 接收业务调用的延迟消息处理结果反馈

**分布式任务调度服务**
- 1.0版本用的elastic-job，2.0切换成了xxl-job

**数据分片**
- 作业数据分片，依赖分布式任务调度服务实现
- elastic-job支持节点添加/删除，主节点选举，重新分片
- xxl-job执行器机器上线或者下线，下次调度时将会重新分配任务

**任务时钟加载作业**
- 由分布式任务调度服务支持，使用数据分片功能，提升系统总吞吐量
- 将未来N分钟内要触发的任务时钟加载到内存进行调度

**任务在内存中的存储和调度**
- 任务加载作业将未来N分钟内触发的任务时钟加载到时间轮中
- 任务调度依靠HashedWheelTimer按照秒级精度精确触发，触发之后加载任务时钟对应的任务，进行轮询发送消息

**数据库**
- 延迟任务持久化，存储任务数据

**延迟任务时钟状态**
![任务时钟状态流转](/Users/caisl/work/program-project/github/delay-task/src/document/image/任务时钟状态流转.jpg)

```
    PREPARE(1, "初始化"),
    LOAD(2, "任务已加载"),
    EXECUTE(3, "已执行"),
    EXPIRE_EXECUTE(4, "过期执行");
```


**延迟任务状态**
![延迟任务状态流转](/Users/caisl/work/program-project/github/delay-task/src/document/image/延迟任务状态流转.jpg)

```
     INIT(1, "初始化"),
     SEND(2, "消息已发放"),
     SUCCESS(3, "业务处理成功"),
     FAIL(4, "业务处理失败"),
     CANCEL(5, "业务取消");
```

### 3.2 数据库设计

底层数据存储主要两张表：
1、delay_task_clock，记录时间轮中需要调度的任务时钟，每个任务时钟可能对应多个任务 1:n
2、delay_task_info，用来存储延迟任务的业务数据信息，包括业务方要消费的消息的tag，topic，以及消息体内容
```
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
```

## 3.3 延迟任务的一生
![延迟任务主要流程](/Users/caisl/work/program-project/github/delay-task/src/document/image/延迟任务处理流程图.jpg)

**提交任务**<br>
1.业务系统通过dubbo接口提交任务
2.判断任务执行时间是否在延迟任务时钟加载周期内，这一步如果不做的话，会导致任务不执行
3.在周期内，则直接添加到时间轮中等待调度，然后落库
4.不在周期内，直接落库

**任务时钟被加载**<br>
1.定时任务按照固定周期分片广播所有注册节点，所有节点执行业务逻辑，将一定时间范围内的延迟任务时钟添加到时间轮，等待时间轮调度

**任务调度**<br>
1.任务调度交由时间轮处理
2.时间轮内部工作线程轮询到对应任务时钟，触发工作线程，根据任务时钟去查询对应时钟下的所有延迟任务，批量发放MQ消息

**任务处理结果反馈**<br>
1.业务系统订阅延迟任务消息
2.收到延迟任务消息之后进行内部的业务逻辑处理，之后调用dubbo接口告知处理结果

**任务取消**<br>
1.业务系统调用dubbo接口取消任务
2.还未执行的任务支持取消，已经执行的任务提示失败


## 3.4 版本迭代记录
|  版本号   | 功能说明  |
|  ----  | ----  |
| release-2.0.0  | 新增任务时钟表，切换分布式任务调度服务为xxl-job(轻量级)，任务调度通过时间轮（O(1)）降低时间复杂度，提升性能 |
| release-1.0.0  | 采用elastic-job + db + DelayQueue（O(logN)） + mq实现核心任务调度功能 |


## 四、服务接入说明
delay-task目前属于本地工程，后续可以做分布式，拆分为client和provider两个模块。通过dubbo框架提供RPC服务。
时钟加载定时任务实现可以看各自公司技术栈，单机环境可以直接用schedule，分布式环境可看具体技术框架都可以改造。

## 五、运行效果

### 5.1 任务添加
```
2021-03-10 16:52:42.929 INFO [pool-1-thread-5] delay_task_operate [addTask success]。json:{"taskTriggerTime":1615366713000,"delayTaskId":1615366362926}
2021-03-10 16:52:42.939 INFO [pool-1-thread-4] delay_task_operate [addTask success]。json:{"taskTriggerTime":1615366713000,"delayTaskId":1615366362943}
2021-03-10 16:52:42.943 INFO [pool-1-thread-3] delay_task_operate [addTask success]。json:{"taskTriggerTime":1615366713000,"delayTaskId":1615366362946}
2021-03-10 16:52:42.947 INFO [pool-1-thread-1] delay_task_operate [addTask success]。json:{"taskTriggerTime":1615366713000,"delayTaskId":1615366362952}
2021-03-10 16:52:42.950 INFO [pool-1-thread-10] delay_task_operate [addTask success]。json:{"taskTriggerTime":1615366713000,"delayTaskId":1615366362957}
2021-03-10 16:52:42.954 INFO [pool-1-thread-8] delay_task_operate [addTask success]。json:{"taskTriggerTime":1615366713000,"delayTaskId":1615366362961}
2021-03-10 16:52:42.959 INFO [pool-1-thread-6] delay_task_operate [addTask success]。json:{"taskTriggerTime":1615366713000,"delayTaskId":1615366362969}
2021-03-10 16:52:43.030 INFO [pool-1-thread-9] delay_task_operate [addTask success]。json:{"taskTriggerTime":1615366713000,"delayTaskId":1615366363040}
2021-03-10 16:52:43.030 INFO [pool-1-thread-7] delay_task_operate [addTask success]。json:{"taskTriggerTime":1615366713000,"delayTaskId":1615366363041}
2021-03-10 16:52:43.031 INFO [pool-1-thread-2] delay_task_operate [addTask success]。json:{"taskTriggerTime":1615366713000,"delayTaskId":1615366363043}

```

### 5.2 延迟任务触发
```
机器一：
2021-03-10 16:55:00.302 INFO [Thread-21] delay_task_operate [loadDelayTaskClockJob启动]。json:{"总分片数":2,"当前分片序号":1}
2021-03-10 16:55:00.306 INFO [Thread-21] delay_task_clock [开始加载时钟到时间轮]。json:{"msg":"执行第1次扫描","curLoopListSize":1}
2021-03-10 16:55:00.307 INFO [Thread-21] delay_task_clock [loadTaskIntoWheelTimer]。json:{"msg":"load clock, clockTriggerTime:1615366713000","currentPendingTimeouts":1}
2021-03-10 16:55:00.317 INFO [Thread-21] delay_task_clock [加载时钟到时间轮结束]。json:{"loadClockNum":1}
2021-03-10 16:56:33.417 INFO [Thread-21]  >>>>>>>>>>> xxl-job JobThread stoped, hashCode:Thread[Thread-21,10,main]
2021-03-10 16:58:33.315 INFO [DelayTaskTriggerThread-1] delay_task_operate [开始处理时间轮任务]。json:{"taskTriggerTime":1615366713000,"shardingIndex":1}
2021-03-10 16:58:33.322 INFO [DelayTaskTriggerThread-1] delay_task_message [sendMsg success]。json:{"delayTaskId":1615366362943,"msgId":"","topic":"mjk_daily_order","timeLoss":322,"tag":"order_auto_timeout"}
2021-03-10 16:58:33.325 INFO [DelayTaskTriggerThread-1] delay_task_message [sendMsg success]。json:{"delayTaskId":1615366362957,"msgId":"","topic":"mjk_daily_order","timeLoss":325,"tag":"order_auto_timeout"}
2021-03-10 16:58:33.327 INFO [DelayTaskTriggerThread-1] delay_task_message [sendMsg success]。json:{"delayTaskId":1615366362961,"msgId":"","topic":"mjk_daily_order","timeLoss":326,"tag":"order_auto_timeout"}
2021-03-10 16:58:33.328 INFO [DelayTaskTriggerThread-1] delay_task_message [sendMsg success]。json:{"delayTaskId":1615366362969,"msgId":"","topic":"mjk_daily_order","timeLoss":328,"tag":"order_auto_timeout"}
2021-03-10 16:58:33.330 INFO [DelayTaskTriggerThread-1] delay_task_message [sendMsg success]。json:{"delayTaskId":1615366363041,"msgId":"","topic":"mjk_daily_order","timeLoss":330,"tag":"order_auto_timeout"}
2021-03-10 16:58:33.331 INFO [DelayTaskTriggerThread-1] delay_task_message [sendMsg success]。json:{"delayTaskId":1615366363043,"msgId":"","topic":"mjk_daily_order","timeLoss":331,"tag":"order_auto_timeout"}
2021-03-10 16:58:33.333 INFO [DelayTaskTriggerThread-1] delay_task_operate [延迟任务执行成功]。json:{"currentPendingTimeouts":0,"taskTriggerTime":1615366713000,"delayTaskSize":6}

机器二：
2021-03-10 16:55:00.138 INFO [Thread-21] delay_task_operate [loadDelayTaskClockJob启动]。json:{"总分片数":2,"当前分片序号":0}
2021-03-10 16:55:00.143 INFO [Thread-21] delay_task_clock [开始加载时钟到时间轮]。json:{"msg":"执行第1次扫描","curLoopListSize":1}
2021-03-10 16:55:00.144 INFO [Thread-21] delay_task_clock [loadTaskIntoWheelTimer]。json:{"msg":"load clock, clockTriggerTime:1615366713000","currentPendingTimeouts":1}
2021-03-10 16:55:00.152 INFO [Thread-21] delay_task_clock [加载时钟到时间轮结束]。json:{"loadClockNum":1}
2021-03-10 16:56:33.233 INFO [Thread-21]  >>>>>>>>>>> xxl-job JobThread stoped, hashCode:Thread[Thread-21,10,main]
2021-03-10 16:58:33.153 INFO [DelayTaskTriggerThread-1] delay_task_operate [开始处理时间轮任务]。json:{"taskTriggerTime":1615366713000,"shardingIndex":0}
2021-03-10 16:58:33.160 INFO [DelayTaskTriggerThread-1] delay_task_message [sendMsg success]。json:{"delayTaskId":1615366362926,"msgId":"","topic":"mjk_daily_order","timeLoss":160,"tag":"order_auto_timeout"}
2021-03-10 16:58:33.163 INFO [DelayTaskTriggerThread-1] delay_task_message [sendMsg success]。json:{"delayTaskId":1615366362946,"msgId":"","topic":"mjk_daily_order","timeLoss":163,"tag":"order_auto_timeout"}
2021-03-10 16:58:33.165 INFO [DelayTaskTriggerThread-1] delay_task_message [sendMsg success]。json:{"delayTaskId":1615366362952,"msgId":"","topic":"mjk_daily_order","timeLoss":164,"tag":"order_auto_timeout"}
2021-03-10 16:58:33.166 INFO [DelayTaskTriggerThread-1] delay_task_message [sendMsg success]。json:{"delayTaskId":1615366363040,"msgId":"","topic":"mjk_daily_order","timeLoss":166,"tag":"order_auto_timeout"}
2021-03-10 16:58:33.168 INFO [DelayTaskTriggerThread-1] delay_task_operate [延迟任务执行成功]。json:{"currentPendingTimeouts":0,"taskTriggerTime":1615366713000,"delayTaskSize":4}
2021-03-10 17:00:00.039 INFO [xxl-rpc, NettyHttpServer-serverHandlerPool-54876659]  >>>>>>>>>>> xxl-job regist JobThread success, jobId:59, handler:com.caisl.dt.job.LoadDelayTaskClockJobHandler@3cae7b8b
2021-03-10 17:00:00.042 INFO [Thread-22] delay_task_operate [loadDelayTaskClockJob启动]。json:{"总分片数":2,"当前分片序号":0}

```

## 六、总结
### 6.1 开发过程中遇到的问题
Q：任务可靠性如何保证？基于内存的调度，应用重启之后，时间轮里面的任务都会丢失<br>
A：应用部署启动成功之后重新触发一次任务调度，将未来N分钟内要过期的任务重新加载到内存中

Q: 延迟任务调度消息重复发放<br>
A: 1、分布式锁控制同一个任务只能发送一次消息 2、RMQ也有可能重复发送消息，业务方需要支持幂等操作

Q：预发环境和正式环境用相同的数据库，环境隔离存在问题<br>
A：延迟信息表添加环境字段参数，进行数据隔离

Q：同个时钟延迟任务并发添加，只添加成功一个，其余因为唯一索引导致添加失败<br>
A：想到两个方案可以解决这个问题
方案一：添加重试机制，优先：对现有设计改动量很小 缺点：高并发占用CPU资源
方案二：去掉delay_task_clock表，默认时间轮每个时钟都有任务，预加载时间轮固定时间周期任务，比如10分钟，然后每10分钟启动定时任务重新添加一遍时钟，通过空间换时间提升性能。优点：性能比方案一好 缺点：无任务的时钟占用空间
综合改造成本和体量分析，采用了方案一进行改造

Q：某一时钟有大量任务触发，可能导致消息发放有一定的延迟<br>
A：系统设计上面采用分片方式尽可能提升系统任务处理速度，原则上这种场景希望业务方以另外的方式去解决，批量逻辑自行处理

Q：在实践过程中发现延迟任务调度使用xxl-job自带的分片功能存在一些问题，xxl-job的分片随着节点上线下线只有任务触发的时候才能通知执行任务的节点，
在时间轮加载了未来5分钟内的任务的时候，分片信息还是上次加载任务触发的时候，在此期间如果节点下线，将会导致下线节点分片任务无法执行<br>
A：这个目前还未想到合适的方案，先通过任务补偿job保证任务都能被调度成功

Q：分片算法比较简单，直接数据库主键mod，分片不是很均匀<br>
A：需要调整下策略，任务表加个分片字段，递增保证分片均匀，取模按照新加的字段

### 6.2 延迟任务演进过程
从2018年末上线实现了这套服务，上线之初暴露了各种问题，也引起了一个比较大的故障，有问题，那就去解决问题，解决着解决着也就经历了多次的演进到现在的一个稳定版本。
19年换了工作，到了一家新的公司继续引用这套模型，在原来基础上进行改造，切换了分布式任务调度服务，然后又做了一些优化
总体来说这套服务是经历过多个行业业务验证的，目前呈现的这个版本跟最初的版本也是相差巨大，属于延迟任务调度的一个经济适用方案实现，美中不足的是没有经历过太大的业务量考验~在日后的各场景业务支持的过程中，相信会不断的慢慢演进！

