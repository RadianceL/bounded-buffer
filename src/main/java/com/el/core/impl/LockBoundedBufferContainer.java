package com.el.core.impl;

import com.el.entity.ExecuteJob;
import com.el.exceptions.BasicBoundedBufferException;
import com.el.worker.ExecutorPool;
import com.el.worker.ExecutorPoolFactory;
import com.el.worker.model.Custom;
import com.el.worker.model.Producer;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author eddie
 * @createTime 2019-06-05
 * @description 基于Lock的生产消费容器
 */
@Slf4j
public class LockBoundedBufferContainer extends AbstractBoundedBufferContainer {

    /**
     * 数据类型
     */
    private final List<ExecuteJob> executeJos;

    /**
     * 最大产品数量
     */
    private int maxProductSize;

    /**
     * 消费者
     */
    private Custom custom;

    /**
     * 生产者
     */
    private Producer producer;

    private Lock lock;

    /**
     * 生产者锁
     */
    private Condition producerCondition;

    /**
     * 消费者锁
     */
    private Condition customCondition;

    /**
     * 多线程优化 避免同一时间多条线程持有该对象 同时触发该对象的状态更改
     */
    private AtomicBoolean shouldRun = new AtomicBoolean(false);

    /**
     * 是否为第一次启动
     */
    private AtomicBoolean isFirstStart = new AtomicBoolean(true);

    /**
     * 线程池
     */
    private static final ExecutorPool EXECUTOR_POOL = ExecutorPoolFactory.getInstance(4, "WaitNotifyBoundedBufferContainer");

    public LockBoundedBufferContainer(int maxProductSize) {
        this(maxProductSize, null, null, null);
        this.setContainerName(String.valueOf(this.hashCode()));
    }

    public LockBoundedBufferContainer(int maxProductSize, String containerName) {
        this(maxProductSize, containerName, null, null);
        this.setContainerName(String.valueOf(this.hashCode()));
    }


    public LockBoundedBufferContainer(int maxProductSize, String containerName, Custom custom, Producer producer) {
        super(containerName);
        lock = new ReentrantLock();
        producerCondition = lock.newCondition();
        customCondition = lock.newCondition();
        this.setContainerName(String.valueOf(this.hashCode()));
        executeJos = new ArrayList<>(maxProductSize);
        this.maxProductSize = maxProductSize;
        this.custom = custom;
        this.producer = producer;
    }

    @Override
    void start0() {
        if (isFirstStart.get()){
            shouldRun.set(true);
            isFirstStart.set(false);
            EXECUTOR_POOL.doWork(this::executeProducer);
            EXECUTOR_POOL.doWork(this::executeCustom);
        }else {
            shouldRun.set(true);
            EXECUTOR_POOL.doWork(this::executeProducer);
            EXECUTOR_POOL.doWork(this::executeCustom);
        }
    }

    @Override
    public void setMaxProductSize(int maxProductSize) {
        this.maxProductSize = maxProductSize;
    }

    @Override
    public void setCustom(Custom custom) {
        this.custom = custom;
    }

    @Override
    public void setProducer(Producer producer) {
        this.producer = producer;
    }

    @Override
    public Custom getCustom() {
        if (Objects.isNull(this.custom)){
            throw new BasicBoundedBufferException("获取消费者异常，消费者不存在！");
        }
        return this.custom;
    }

    @Override
    public Producer getProducer() {
        if (Objects.isNull(this.producer)){
            throw new BasicBoundedBufferException("获取生产者异常，生产者不存在！");
        }
        return this.producer;
    }

    private void executeProducer() throws InterruptedException {
        log.info("进入生产者线程");
        lock.lock();
        try {
            if (!shouldRun.get()) {
                Thread.yield();
                TimeUnit.SECONDS.sleep(5);
            }
            while (shouldRun.get()) {
                if (executeJos.size() >= maxProductSize) {
                    producerCondition.await();
                } else {
                    ExecuteJob product = getProducer().manufacture();
                    executeJos.add(product);
                    customCondition.signalAll();
                }

            }
        }finally {
            lock.unlock();
        }
    }
    @Override
    void stop0(){
        //不必要立即让其他线程可见 优化程序 减少内存屏障
        shouldRun.set(false);
    }

    private void executeCustom() throws InterruptedException {
        log.info("进入消费者线程");
        lock.lock();
        if (!shouldRun.get()){
            Thread.yield();
            TimeUnit.SECONDS.sleep(5);
        }
        try {
            while (shouldRun.get()) {
                if (executeJos.size() > 0) {
                    ExecuteJob executeJob = executeJos.remove(0);
                    getCustom().consumption(executeJob);
                    producerCondition.signalAll();
                } else {
                    customCondition.await();
                }
            }
        }finally {
            lock.unlock();
        }

    }
}
