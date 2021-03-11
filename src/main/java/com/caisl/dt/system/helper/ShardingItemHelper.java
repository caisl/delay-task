package com.caisl.dt.system.helper;

import org.springframework.stereotype.Service;

/**
 * ShardingItemHelper
 *
 * @author caisl
 * @since 2019-05-08
 */
@Service
public class ShardingItemHelper {

    private int index = 0;

    private int indexTotal = 1;

    public void initShardingParam(int index, int indexTotal){
        if(indexTotal == 0){
            return;
        }
        this.index = index;
        this.indexTotal = indexTotal;
    }


    public int getIndex() {
        return index;
    }

    public int getIndexTotal() {
        return indexTotal;
    }
}
