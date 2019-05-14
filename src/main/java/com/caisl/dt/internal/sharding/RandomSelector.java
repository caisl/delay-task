package com.caisl.dt.internal.sharding;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * RandomSelector
 *
 * @author caisl
 * @since 2019-05-09
 */
public class RandomSelector extends AbstractShardingIdSelector {
    @Override
    protected Integer doSelect(List<Integer> shardingIds) {
        return ThreadLocalRandom.current().nextInt(shardingIds.size());
    }
}
