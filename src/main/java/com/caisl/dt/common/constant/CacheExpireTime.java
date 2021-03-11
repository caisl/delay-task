package com.caisl.dt.common.constant;

/**
 * CacheExpireTime
 *
 * @author caisl
 * @since 2021/3/02
 */
public interface CacheExpireTime {

    /**
     * 2秒
     */
    int EXPIRE_TWO_SECOND = 2;
    /**
     * 3秒
     */
    int EXPIRE_THREE_SECOND = 3;
    /**
     * 一分钟
     */
    int EXPIRE_MINUTE = 60;

    /**
     * 两分钟
     */
    int EXPIRE_TWO_MINUTE = 120;

    /**
     * 5分钟
     */
    int EXPIRE_FIVE_MINUTE = 300;

    /**
     * 10分钟
     */
    int EXPIRE_TEN_MINUTE = 600;

    /**
     * 1小时
     */
    int EXPIRE_HOUR = 60 * 60;

    /**
     * 一天
     */
    int EXPIRE_DAY = 24 * 60 * 60;

    /**
     * 一周
     */
    int EXPIRE_WEEK = 24 * 60 * 60 * 7;

}
