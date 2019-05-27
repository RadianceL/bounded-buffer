package com.el.worker.model;

import com.el.entity.ExecuteJob;

/**
 * @author eddie
 * @createTime 2019-05-27
 * @description 消费者
 */
public interface Custom {

    /**
     * 消费一个产品
     */
    void consumption(ExecuteJob e);

}
