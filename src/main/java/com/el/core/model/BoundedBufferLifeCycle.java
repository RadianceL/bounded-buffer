package com.el.core.model;

import com.el.entity.ExecuteJob;
import com.el.status.BoundedBufferLifeCycleStatus;

import java.util.List;

/**
 * @author eddie
 * @createTime 2019-05-26
 * @description 生产消费容器生命周期方法
 */
public interface BoundedBufferLifeCycle {

    /**
     * 开始执行
     */
    void start();

    /**
     * 暂停生产消费
     */
    void stop();

    /**
     * 停止服务 停止生产 等待消费完成
     */
    void shutDown();

    /**
     * 获取当前运行状态
     * @return
     */
    BoundedBufferLifeCycleStatus getStatus();

    /**
     * 停止服务 停止生产 立即结束 返回剩余待执行任务
     */
    List<? extends ExecuteJob> shutDownImmediately();
}
