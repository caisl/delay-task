package com.caisl.dt.common.dataobject;

import lombok.Data;

import java.io.Serializable;

/**
 * BaseDO
 *
 * @author caisl
 * @since 2019-03-12
 */
@Data
public abstract class BaseDO implements Serializable {
    private static final long serialVersionUID = 4567582374248772921L;
    /**
     * 主键
     */
    private Long id;
    /**
     * 是否有效
     */
    private Integer isValid = 1;

    /**
     * 版本号
     */
    private Integer lastVer = 1;

    /**
     * 创建时间
     */
    private Long gmtCreate = System.currentTimeMillis();

    /**
     * 更新时间
     */
    private Long gmtUpdate = System.currentTimeMillis();

}
