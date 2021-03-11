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
    private ShardingItemHelper shardingItemHelper;

    @Override
    public Integer select(boolean isLocalNode) {
        return 0;
    }

    /**
     * 子类自己实现分区ID选择算法
     *
     * @param shardingIds
     * @return
     */
    protected abstract Integer doSelect(List<Integer> shardingIds);

}
