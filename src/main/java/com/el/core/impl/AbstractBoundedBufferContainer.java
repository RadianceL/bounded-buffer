package com.el.core.impl;

import com.el.core.model.BasicBoundedBufferContainerService;
import com.el.entity.ExecuteJob;
import com.el.status.BoundedBufferLifeCycleStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author eddie
 * @createTime 2019-05-26
 * @description 基础生产消费保证容器实现
 */
@Slf4j
public abstract class AbstractBoundedBufferContainer implements BasicBoundedBufferContainerService {

    /**
     * 运行时状态
     */
    private BoundedBufferLifeCycleStatus status;

    /**
     * 容器名称 别名
     */
    private String containerName;


    public AbstractBoundedBufferContainer(String containerName){
        this.containerName = containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public String getContainerName() {
        return this.containerName;
    }

    @Override
    public void start() {
        this.status = BoundedBufferLifeCycleStatus.STARTING;
        start0();
        this.status = BoundedBufferLifeCycleStatus.RUNNING;
    }

    @Override
    public void stop() {
        stop0();
        this.status = BoundedBufferLifeCycleStatus.SUSPEND;
    }


    @Override
    public void shutDown() {
        this.status = BoundedBufferLifeCycleStatus.SHUTDOWN;
        shutDown0();
        this.status = BoundedBufferLifeCycleStatus.STOPED;
    }

    @Override
    public BoundedBufferLifeCycleStatus getStatus() {
        return this.status;
    }

    @Override
    public List<ExecuteJob> shutDownImmediately() {
        //立即停止 无停止中状态
        this.status = BoundedBufferLifeCycleStatus.STOPED;
        return shutDownImmediately0();
    }

    /**
     * 模版方法 开始过程调用
     */
    void start0(){}

    /**
     * 模版方法 暂停调用
     */
    void stop0(){}

    /**
     * 模版方法 结束调用
     */
    void shutDown0(){}

    /**
     * 模版方法 立即结束调用
     */
    List<ExecuteJob> shutDownImmediately0(){return null;}
}
