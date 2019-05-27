package com.el.core.impl;

import com.el.entity.ExecuteJob;
import com.el.exceptions.BasicBoundedBufferException;
import com.el.worker.ExecutorPool;
import com.el.worker.model.Custom;
import com.el.worker.model.Producer;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author eddie
 * @createTime 2019-05-27
 * @description 基于wait/notify实现的生产消费模型
 */
@Slf4j
public class WaitNotifyBoundedBufferContainer extends AbstractBoundedBufferContainer {

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

    /**
     * 默认执行睡眠时间
     */
    private static final int DEFAULT_SLEEP_SECOND_TIME = 2;

    /**
     * 线程池
     */
    private static final ExecutorPool EXECUTOR_POOL = ExecutorPool.getInstance(4, "WaitNotifyBoundedBufferContainer");

    public WaitNotifyBoundedBufferContainer(int maxProductSize) {
        this(maxProductSize, null, null, null);
        this.setContainerName(String.valueOf(this.hashCode()));
    }

    public WaitNotifyBoundedBufferContainer(int maxProductSize, String containerName) {
        this(maxProductSize, containerName, null, null);
        this.setContainerName(String.valueOf(this.hashCode()));
    }


    public WaitNotifyBoundedBufferContainer(int maxProductSize, String containerName, Custom custom, Producer producer) {
        super(containerName);
        this.setContainerName(String.valueOf(this.hashCode()));
        executeJos = new ArrayList<>(maxProductSize);
        this.maxProductSize = maxProductSize;
        this.custom = custom;
        this.producer = producer;
    }

    public void setCustom(Custom custom) {
        this.custom = custom;
    }

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

    public void setMaxProductSize(int maxProductSize) {
        this.maxProductSize = maxProductSize;
    }

    @Override
    void start0() {
        EXECUTOR_POOL.doWork(() -> {
            log.info("进入生产者线程");
            while (true) {
                synchronized (executeJos) {
                    log.info("生产者执行生产动作");
                    if (executeJos.size() >= maxProductSize) {
                        log.info("以达到生产上线，等待消费者消费");
                        executeJos.wait();
                    }
                    ExecuteJob product = getProducer().manufacture();
                    executeJos.add(product);
                    log.info("生产者生产对象 {}, 第{}个", product, executeJos.size());
                    executeJos.notifyAll();
                }
            }

        });
        EXECUTOR_POOL.doWork(() -> {
            log.info("进入消费者线程");
            while (true) {
                synchronized (executeJos) {
                    log.info("消费者执行消费动作");
                    if (executeJos.size() == 0) {
                        log.info("无可消费内容");
                        executeJos.wait();
                    }
                    log.info("开始消费");
                    ExecuteJob executeJob = executeJos.remove(0);
                    getCustom().consumption(executeJob);
                    log.info("消费者消费对象 {}, 剩余{}个", executeJob, executeJos.size());
                    executeJos.notifyAll();
                }
            }
        });
    }
}
