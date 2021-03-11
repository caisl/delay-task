package com.caisl.dt.system.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * XxlJobConfig
 *
 * @author caisl
 * @since 2021/2/25
 */
@Configuration
@Data
public class XxlJobConfig {
    /**
     * job中心地址
     */
    @Value("${xxl.job.admin.addresses}")
    private String adminAddresses;

    /**
     * 应用名，一个应用对应一个job执行器
     */
    @Value("${xxl.job.executor.appname}")
    private String appName;

    /**
     * job中心的AccessToken
     */
    @Value("${xxl.job.accessToken}")
    private String accessToken;

    /**
     * 日志path
     */
    @Value("${xxl.job.executor.logpath}")
    private String logPath;

    /**
     * 日志保留天数
     */
    @Value("${xxl.job.executor.logretentiondays}")
    private Integer logRetentionDays;

    @Bean(destroyMethod = "destroy")
    public XxlJobSpringExecutor xxlJobExecutor() {
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(adminAddresses);
        xxlJobSpringExecutor.setAppName(appName);
        //xxlJobSpringExecutor.setIp(); //ip不配置自动获取机器ip
        //xxlJobSpringExecutor.setPort(0); //port不设置或小于等于0自动分配
        xxlJobSpringExecutor.setAccessToken(accessToken);
        xxlJobSpringExecutor.setLogPath(logPath);
        xxlJobSpringExecutor.setLogRetentionDays(logRetentionDays);
        return xxlJobSpringExecutor;
    }
}
