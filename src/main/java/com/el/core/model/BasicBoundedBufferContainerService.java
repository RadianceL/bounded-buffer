package com.el.core.model;

import com.el.worker.model.Custom;
import com.el.worker.model.Producer;

/**
 * @author eddie
 * @createTime 2019-05-26
 * @description 基础生产消费保证容器接口
 */
public interface BasicBoundedBufferContainerService extends BoundedBufferLifeCycle{
    /**
     * 获取消费者
     * @return
     */
    Custom getCustom();

    /**
     * 获取生产者
     * @return
     */
    Producer getProducer();

    /**
     * 设置一个消费者
     * @param custom
     */
    void setCustom(Custom custom);

    /**
     * 设置一个生产者
     * @param producer
     */
    void setProducer(Producer producer);

}
