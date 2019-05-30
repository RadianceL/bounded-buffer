package com.el.worker;

import com.el.exceptions.BasicBoundedBufferException;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.Objects;
import java.util.concurrent.*;

/**
 * @author eddie
 * @createTime 2019-01-17
 * @description 线程池
 */
public final class ExecutorPool {

    /**
     * 线程池
     */
    private ExecutorService service;

    /**
     * 信号锁
     */
    private Semaphore semaphore;

    /**
     * MAX线程因子
     */
    private static final int FACTOR = 2;

    /**
     * 线程池名字
     */
    private String threadName;


    private static ExecutorPool executorPool;

    /**
     * 私有构造函数
     * @param threadTotal   核心线程数
     * @param nameFormat    线程名称格式
     */
    private ExecutorPool(int threadTotal, String nameFormat){
        ThreadFactory factory = new ThreadFactoryBuilder().setNameFormat(nameFormat).build();
        this.service = new ThreadPoolExecutor(threadTotal, threadTotal * FACTOR + 1, 5L, TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<>(256), factory, new ThreadPoolExecutor.AbortPolicy());
        //初始化信号量
        this.semaphore = new Semaphore(threadTotal);
    }

    /**
     * 执行工作
     * @param action    lambda表达式，要做的工作
     */
    public void doWork(WorkAction action) {
        try {
            service.execute(() -> {
                try {
                    semaphore.acquire();
                    action.execute();
                } catch (InterruptedException e) {
                    throw new BasicBoundedBufferException("线程异常: {}", e.getMessage());
                }finally {
                    semaphore.release();
                }
            });
        }catch (BasicBoundedBufferException e){
            this.shutDown();
            throw new BasicBoundedBufferException(e);
        }
    }

    /**
     * 关闭一个线程池
     */
    public void shutDown(){
        try {
            service.shutdown();
            service.awaitTermination(Integer.MAX_VALUE, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            System.out.println("线程池关闭异常" + e.getMessage());
        }
    }

    public boolean isShutdown(){
        return service.isShutdown();
    }

    private void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getThreadName() {
        return threadName;
    }

    /**
     * 双重检查单例
     * @param threadTotal   核心线程数 同时可执行线程数
     * @param poolName      线程池名称
     * @return
     */
    public static ExecutorPool getInstance(int threadTotal, String poolName){
        if(Objects.isNull(executorPool)){
            synchronized (ExecutorPool.class){
                if (Objects.isNull(executorPool)) {
                    ExecutorPool newExecutorPool = new ExecutorPool(threadTotal, poolName.concat("-Thread-%d"));
                    newExecutorPool.setThreadName(poolName);
                    executorPool = newExecutorPool;
                }
            }
        }
        return executorPool;
    }

}
