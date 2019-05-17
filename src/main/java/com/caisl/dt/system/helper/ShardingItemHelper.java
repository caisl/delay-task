package com.caisl.dt.system.helper;

import com.caisl.dt.job.DelayTaskLoadConfig;
import com.dangdang.ddframe.job.exception.JobConfigurationException;
import com.dangdang.ddframe.job.lite.internal.sharding.ShardingService;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.dangdang.ddframe.job.util.config.ShardingItemParameters;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * ShardingItemHelper
 *
 * @author caisl
 * @since 2019-05-08
 */
@Service
public class ShardingItemHelper {
    private static final String PARAMETER_DELIMITER = ",";

    private static final String KEY_VALUE_DELIMITER = "=";

    @Resource
    private ZookeeperRegistryCenter regCenter;

    @Resource
    private DelayTaskLoadConfig delayTaskLoadConfig;


    private final ShardingService shardingService = new ShardingService(regCenter, delayTaskLoadConfig.getDelayTaskLoadJobName());


    /**
     * 获取分片ID集合
     *
     * @param isLocalNode
     * @return
     */
    public List<Integer> getShardingIds(boolean isLocalNode) {
        if (isLocalNode) {
            return getLocalShardingIds();
        }
        return getJobShardingIds();
    }

    /**
     * 获取当前节点作业实例的分片ID集合
     *
     * @return
     */
    public List<Integer> getLocalShardingIds() {
        return shardingService.getLocalShardingItems();
    }

    /**
     * 获取当前作业设置的分片ID集合
     *
     * @return
     */
    public List<Integer> getJobShardingIds() {
      /*  String originalShardingItemParameters = delayTaskLoadConfig.getShardingItemParameters();
        if (StringUtils.isBlank(originalShardingItemParameters)) {
            return Collections.EMPTY_LIST;
        }
        String[] shardingItemParameters = originalShardingItemParameters.split(PARAMETER_DELIMITER);
        List<Integer> shardingIds = Lists.newArrayListWithCapacity(shardingItemParameters.length);
        String[] pair = delayTaskLoadConfig.getShardingItemParameters().trim().split(KEY_VALUE_DELIMITER);
        for (String each : shardingItemParameters) {
            shardingIds.add(parse(each, originalShardingItemParameters));
        }
        return shardingIds;*/
        Map<Integer, String> shardingItemParameterMap = new ShardingItemParameters(delayTaskLoadConfig.getShardingItemParameters()).getMap();
        return Lists.newArrayList(shardingItemParameterMap.keySet());

    }

    /**
     * 获取shardingId
     *
     * @param shardingItemParameter
     * @param originalShardingItemParameters
     * @return
     */
    private Integer parse(final String shardingItemParameter, final String originalShardingItemParameters) {
        String[] pair = shardingItemParameter.trim().split(KEY_VALUE_DELIMITER);
        if (2 != pair.length) {
            throw new JobConfigurationException("Sharding item parameters '%s' format error, should be int=xx,int=xx", originalShardingItemParameters);
        }
        try {
            return Integer.parseInt(pair[0].trim());
        } catch (final NumberFormatException ex) {
            throw new JobConfigurationException("Sharding item parameters key '%s' is not an integer.", pair[0]);
        }
    }

}
