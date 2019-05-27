package com.el.worker.model;

import com.el.entity.ExecuteJob;

/**
 * @author eddie
 * @createTime 2019-05-27
 * @description 生产者
 */
public interface Producter<E extends ExecuteJob> {

    /**
     * 生产一个产品
     */
    E manufacture();




}
