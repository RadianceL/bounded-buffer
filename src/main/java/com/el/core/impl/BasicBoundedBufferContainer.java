package com.el.core.impl;

import com.el.core.model.BasicBoundedBufferContainerService;
import com.el.core.model.BoundedBufferLifeCycle;
import com.el.entity.ExecuteJob;
import com.el.status.BoundedBufferLifeCycleStatus;
import com.el.worker.model.Custom;
import com.el.worker.model.Producter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @author eddie
 * @createTime 2019-05-26
 * @description 基础生产消费保证容器实现
 */
@Slf4j
public abstract class BasicBoundedBufferContainer<E extends ExecuteJob> implements BasicBoundedBufferContainerService, BoundedBufferLifeCycle {

    /**
     * 运行时状态
     */
    private BoundedBufferLifeCycleStatus status;

    /**
     * 最大产品数量
     */
    private int maxProductSize;

    /**
     * 容器名称 别名
     */
    private String containerName;

    /**
     * 数据类型
     */
    private final List<ExecuteJob> executeJos;

    public void setMaxProductSize(int maxProductSize) {
        this.maxProductSize = maxProductSize;
    }

    public BasicBoundedBufferContainer(int maxProductSize, String containerName){
        this.maxProductSize = maxProductSize;
        this.containerName = containerName;
        executeJos = new ArrayList<>(maxProductSize);
    }

    public BasicBoundedBufferContainer(int maxProductSize){
        this.maxProductSize = maxProductSize;
        this.containerName = String.valueOf(this.hashCode());
        executeJos = new ArrayList<>(maxProductSize);
    }


    public String getContainerName() {
        return containerName;
    }

    @Override
    public void start() {
        this.status = BoundedBufferLifeCycleStatus.STARTING;

        start0();
        try {
            run();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
    public List<? extends ExecuteJob> shutDownImmediately() {
        //立即停止 无停止中状态
        this.status = BoundedBufferLifeCycleStatus.STOPED;
        shutDownImmediately0();
        return this.executeJos;
    }

    private void run() throws InterruptedException {
        while (true) {
            synchronized (executeJos) {
                if (executeJos.size() == maxProductSize) {
                    executeJos.wait();
                }
                ExecuteJob product = getProducter().manufacture();
                executeJos.add(product);
                log.info("生产者生产对象 {}, 第{}个", product, executeJos.size());
                executeJos.notifyAll();
            }
        }


    }

    /**
     * 模版方法 开始过程回调
     */
    void start0(){}

    /**
     * 模版方法 暂停回调
     */
    void stop0(){}

    /**
     * 模版方法 结束回调
     */
    void shutDown0(){}

    /**
     * 模版方法 立即结束回调
     */
    void shutDownImmediately0(){}

    /**
     * 获取消费者
     * @return
     */
    public abstract Custom<E> getCustom();

    /**
     * 获取生产者
     * @return
     */
    public abstract Producter<E> getProducter();

}
