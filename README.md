# delay-task：延迟任务调度系统

## 一、延迟任务需求

### 1.1 需求描述
笔者接触过一些营销业务场景，比如说：
- 用户注册未登录过APP第二天早上10点发一条营销短信促活
- 红包过期前两天短信通知，下午16:00发送
- 等等定时任务处理业务。

采用的技术方案是定时任务扫数据汇总表，分页读取一定数量然后处理
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


## 二、技术方案调研
- java.util.Timer + java.util.TimerTask
- java.util.concurrent.ScheduledExecutorService
- java.util.concurrent.DelayQueue
- 数据库轮询
- 时间轮
- redis过期键通知
- Quartz
- rocketMQ中的延时队列

## 三、系统架构（elastic-job + db + delayQueue + mq）
延迟任务调度系统提供统一的任务操作接口给业务方调用，业务方可以提交任务，取消任务，查询任务状态。
调度服务属于底层应用，因此采用MQ的方式解耦，所有触发的延迟任务都通过消息的方式发送给业务消费方，
由消费方控制流量，业务幂等。同时也保证了任务的重试机制。

### 3.1 整体架构

![整体架构](https://github.com/caisl/delay-task/blob/master/src/document/image/整体架构.jpg)


**业务调用方**
- 业务方在需要延迟任务的时候调用延迟任务服务操作任务
- 触发的延迟任务会放到MQ消息队列里面，由业务方自行消费
- 业务方消费消息处理完成之后，调用延迟任务服务通知处理结果

**延迟任务节点**
- 以dubbo方式提供延迟任务接口供业务方操作，用于添加延迟任务，取消任务，反馈任务处理结果。
- 集成elastic-job提供数据分片功能，每个节点按照对应分片从数据库加载即将触发的延迟任务放到内存中
- 任务调度触发的延迟任务发送到MQ消息队列中
- 接收业务调用的延迟消息处理结果反馈

**Zookeeper**
- elastic-job注册中心，存储作业信息

**elastic-job**
- 高可用的分布式任务调度系统
- 注册任务实例信息和分片信息到zk上

**数据分片**
- elastic-job作业数据分片
- 节点添加/删除，主节点选举，重新分片

**任务加载作业**
- 由elastic-job实现，使用数据分片功能，提升系统总吞吐量
- 将未来N分钟内要触发的任务加载到内存中

**任务在内存中的存储和调度**
- 任务加载作业将未来N分钟内触发的任务加载到内存队列DelayQueue
- 任务调度依靠DelayQueue精确触发

**数据库**
- 延迟任务持久化，存储任务数据

**延迟任务状态**
```
    INIT(1, "初始化"),
    LOAD(2, "任务已加载"),
    SENDING(3, "消息已发放"),
    SUCCESS(4, "业务处理成功"),
    FAIL(5, "业务处理失败"),
    CANCEL(6, "业务取消");
```

### 3.2 数据库设计

![数据库](https://github.com/caisl/delay-task/blob/master/src/document/image/ER.jpg)
```
CREATE TABLE `delay_task` (
  `delay_task_id` varchar(32) NOT NULL COMMENT '任务ID',
  `sharding_id` tinyint(4) NOT NULL COMMENT '分片ID',
  `topic` varchar(100) NOT NULL COMMENT '消息topic',
  `tag` varchar(100) NOT NULL COMMENT '消息tag',
  `params` varchar(1000) NOT NULL COMMENT '参数',
  `trigger_time` bigint(19) NOT NULL COMMENT '执行时间',
  `status` tinyint(4) NOT NULL COMMENT '任务状态：1.初始化 2.任务已加载 3.消息已发放 4.业务处理成功 5.业务处理失败',
  `extend_field` varchar(100) NOT NULL COMMENT '扩展属性',
  `create_time` bigint(20) NOT NULL COMMENT '创建时间',
  `op_time` bigint(20) NOT NULL COMMENT '最近一次更新时间',
  `last_ver` int(10) NOT NULL COMMENT '版本号',
  `is_valid` tinyint(2) NOT NULL DEFAULT '1' COMMENT '是否有效 0-失效 1-有效',
  PRIMARY KEY (`delay_task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='延迟任务表'
```
数据库设计就一张表delay_task，用来存储延迟任务的数据，包括业务方要消费的消息的tag，topic，以及消息体内容


## 四、服务接入说明
delay-task目前属于本地工程，后续可以做分布式，拆分为client和provider两个模块。通过dubbo框架提供RPC服务。

## 五、运行效果
```
2019-05-31 15:24:34.717 INFO [inner-job-delayTaskLoadJob-1] job [DelayTaskLoadJob]。json:{"thread_id":32,"当前分片项":0,"任务总片数":3}
2019-05-31 15:24:34.719 INFO [inner-job-delayTaskLoadJob-2] job [DelayTaskLoadJob]。json:{"thread_id":33,"当前分片项":1,"任务总片数":3}
2019-05-31 15:24:34.721 INFO [inner-job-delayTaskLoadJob-3] job [DelayTaskLoadJob]。json:{"thread_id":34,"当前分片项":2,"任务总片数":3}
2019-05-31 15:24:34.722 INFO [inner-job-delayTaskLoadJob-1] job 开始执行第1次扫描
2019-05-31 15:24:34.725 INFO [inner-job-delayTaskLoadJob-3] job 开始执行第1次扫描
2019-05-31 15:24:34.727 INFO [inner-job-delayTaskLoadJob-2] job 开始执行第1次扫描
2019-05-31 15:24:34.884 INFO [inner-job-delayTaskLoadJob-2] job <====DelayTaskLoad Job End====>1559287474884cost:266
2019-05-31 15:24:34.899 INFO [inner-job-delayTaskLoadJob-1] job <====DelayTaskLoad Job End====>1559287474899cost:302
2019-05-31 15:24:34.904 INFO [inner-job-delayTaskLoadJob-3] job <====DelayTaskLoad Job End====>1559287474904cost:289
2019-05-31 15:24:39.077 INFO [DelayTaskTrigger-4] delay_task_message [sendMsg]。json:{"获取到延迟任务：":1559287474070,"当前时间：":"2019-05-31 15:24:39","任务调度线程：":"DelayTaskTrigger-4","任务触发时间：":"2019-05-31 15:24:39"}
2019-05-31 15:24:39.078 INFO [DelayTaskTrigger-1] delay_task_message [sendMsg]。json:{"获取到延迟任务：":1559287474070,"当前时间：":"2019-05-31 15:24:39","任务调度线程：":"DelayTaskTrigger-1","任务触发时间：":"2019-05-31 15:24:39"}
2019-05-31 15:24:39.085 INFO [DelayTaskTrigger-3] delay_task_message [sendMsg]。json:{"获取到延迟任务：":1559287474070,"当前时间：":"2019-05-31 15:24:39","任务调度线程：":"DelayTaskTrigger-3","任务触发时间：":"2019-05-31 15:24:39"}
2019-05-31 15:24:39.089 INFO [DelayTaskTrigger-2] delay_task_message [sendMsg]。json:{"获取到延迟任务：":1559287474070,"当前时间：":"2019-05-31 15:24:39","任务调度线程：":"DelayTaskTrigger-2","任务触发时间：":"2019-05-31 15:24:39"}
2019-05-31 15:24:39.665 INFO [DelayTaskTrigger-1] delay_task_message [sendMsg]。json:{"获取到延迟任务：":1559287474665,"当前时间：":"2019-05-31 15:24:39","任务调度线程：":"DelayTaskTrigger-1","任务触发时间：":"2019-05-31 15:24:39"}
2019-05-31 15:24:39.676 INFO [DelayTaskTrigger-1] delay_task_message [sendMsg]。json:{"获取到延迟任务：":1559287474665,"当前时间：":"2019-05-31 15:24:39","任务调度线程：":"DelayTaskTrigger-1","任务触发时间：":"2019-05-31 15:24:39"}
2019-05-31 15:24:39.681 INFO [DelayTaskTrigger-4] delay_task_message [sendMsg]。json:{"获取到延迟任务：":1559287474665,"当前时间：":"2019-05-31 15:24:39","任务调度线程：":"DelayTaskTrigger-4","任务触发时间：":"2019-05-31 15:24:39"}
...
```

## 六、总结
### 6.1 开发过程中遇到的问题
1.高可用如何保证：
- 应用重启之后，队列里面丢失的任务如何快速加载

<br>解决方案：
- 应用部署成功之后重新触发一次调度任务，将未来N分钟内要过期的任务加载到内存中

2.如何获取当前节点的数据分片
- 延迟任务加载到延迟队列中时，需要当前服务器的分片数据
- 作业初始化之后还未进行数据分片,只是设置了需要分片的标识
- 服务启动成功之后需要触发一次任务调度

<br>解决方案：
- 应用部署成功之后重新触发一次调度任务，让主节点进行数据分片

3.任务重复加载到延迟队列中
- 服务重启过程加载作业在另外一台服务器触发调度，数据重新分片，重启成功的服务器而后又重新调用了加载任务，数据重新分片，导致任务重复加载。

<br>解决方案：
- 只允许一条任务执行成功，分布式锁控制+业务方幂等
- 任务触发的时候重新检验是否属于该节点处理的分片，不属于则不做处理