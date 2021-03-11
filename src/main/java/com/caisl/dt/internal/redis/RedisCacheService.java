package com.caisl.dt.internal.redis;

import org.springframework.stereotype.Component;

/**
 * RedisService
 *
 * @author caisl
 * @since 2019-05-21
 */
@Component
public class RedisCacheService {

    public boolean setnx(String key, int expireSecond, String value){
        //TODO 各自框架实现
        return true;
    }

    public String get(String key){
        //TODO 各自框架实现
        return "";
    }

    public long del(String key){
        //TODO 各自框架实现
        return 0;
    }
}
