package com.el.core.impl;

import com.el.core.model.BasicBoundedBufferContainerService;
import com.el.entity.ExecuteJob;
import com.el.exceptions.BasicBoundedBufferException;
import com.el.exceptions.BoundedBufferStatusException;
import com.el.exceptions.BoundedBufferThreadShutdownExcetption;
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
     * 运行时状态 初始化为暂停状态
     */
    private BoundedBufferLifeCycleStatus status = BoundedBufferLifeCycleStatus.SUSPEND;

    /**
     * 容器名称 别名
     */
    private String containerName;


    public AbstractBoundedBufferContainer(String containerName){
        this.containerName = containerName;
    }

    void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    String getContainerName() {
        return this.containerName;
    }

    @Override
    public synchronized void start() {
        try {
            if (this.status == BoundedBufferLifeCycleStatus.RUNNING){
                log.warn("容器[{}]已处于启动状态", this.getContainerName());
                return;
            }
            if (this.status == BoundedBufferLifeCycleStatus.STOPED){
                throw new BoundedBufferStatusException("该容器[{}]状态:[{}]，无法执行启动，只有初始状态 && 暂停状态可以执行启动", this.getContainerName(), this.status.getDesc());
            }
            this.status = BoundedBufferLifeCycleStatus.STARTING;
            start0();
            this.status = BoundedBufferLifeCycleStatus.RUNNING;
        }catch (BasicBoundedBufferException e){
            this.stop();
            this.status = BoundedBufferLifeCycleStatus.STOPED;
            throw new BoundedBufferStatusException("[{}]启动失败，case : [{}]", this.getContainerName(), e.getMessage());
        }
    }

    @Override
    public synchronized void stop() {
        if (this.status == BoundedBufferLifeCycleStatus.RUNNING){
            Thread.yield();
            stop0();
            this.status = BoundedBufferLifeCycleStatus.SUSPEND;
        }else {
            throw new BoundedBufferThreadShutdownExcetption("该容器[{}]状态:[{}]，无法暂停，只有运行中状态可以暂停", this.getContainerName(), this.status.getDesc());
        }
    }


    @Override
    public synchronized void shutDown() {
        if (this.status == BoundedBufferLifeCycleStatus.RUNNING || this.status == BoundedBufferLifeCycleStatus.SUSPEND) {
            Thread.yield();
            this.status = BoundedBufferLifeCycleStatus.SHUTDOWN;
            shutDown0();
            this.status = BoundedBufferLifeCycleStatus.STOPED;
        }else {
            throw new BoundedBufferThreadShutdownExcetption("该容器[{}]状态:[{}]，无法停止，只有运行中 && 暂停状态可以停止", this.getContainerName(), this.status.getDesc());
        }
    }

    @Override
    public synchronized List<ExecuteJob> shutDownImmediately() {
        if (this.status == BoundedBufferLifeCycleStatus.RUNNING || this.status == BoundedBufferLifeCycleStatus.SUSPEND) {
            //立即停止 无停止中状态
            this.status = BoundedBufferLifeCycleStatus.STOPED;
            return shutDownImmediately0();
        }else {
            throw new BoundedBufferThreadShutdownExcetption("该容器[{}]状态:[{}]，无法立即停止，只有运行中 && 暂停状态可以立即停止", this.getContainerName(), this.status.getDesc());
        }

    }

    @Override
    public BoundedBufferLifeCycleStatus getStatus() {
        return this.status;
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
