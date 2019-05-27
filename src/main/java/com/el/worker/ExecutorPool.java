package com.el.worker;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.*;

/**
 * @author eddie
 * @createTime 2019-01-17
 * @description 线程池
 */
public abstract class ExecutorPool {

    /**
     * 线程池
     */
    private ExecutorService service;

    /**
     * 信号锁
     */
    private Semaphore semaphore;

    public ExecutorPool(int threadTotal){
        ThreadFactory factory = new ThreadFactoryBuilder().setNameFormat("BoundedBuffer-Thread-%d").build();
        this.service = new ThreadPoolExecutor(5, 50, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<>(1024), factory, new ThreadPoolExecutor.AbortPolicy());
        //信号量
        this.semaphore = new Semaphore(threadTotal);
    }

    public void doWork(final Object message) {
        service.execute(() -> {
            try {
                semaphore.acquire();
                fuck(message);
                semaphore.release();
            } catch (InterruptedException e) {
                System.out.println("exception" +  e.getMessage());
            }
        });
    }

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

    /**
     * 执行具体任务
     * @param message
     * @return
     */
    public abstract void fuck(Object message);
}
