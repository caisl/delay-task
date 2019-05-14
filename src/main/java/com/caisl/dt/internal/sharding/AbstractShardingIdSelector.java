package com.caisl.dt.internal.sharding;

import com.caisl.dt.system.helper.ShardingItemHelper;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * AbstractShardingIdSelector
 *
 * @author caisl
 * @since 2019-05-09
 */
public abstract class AbstractShardingIdSelector implements ShardingIdSelector {

    @Resource
    ShardingItemHelper shardingItemHelper;

    @Override
    public Integer select() {
        List<Integer> shardingIds = shardingItemHelper.getShardingIds();
        if (CollectionUtils.isEmpty(shardingIds)) {
            return null;
        }
        if (shardingIds.size() == 0) {
            return shardingIds.get(0);
        }
        return doSelect(shardingIds);
    }


    protected abstract Integer doSelect(List<Integer> shardingIds);

}
