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


    private static ConcurrentHashMap<String, ExecutorPool> singletonCache = new ConcurrentHashMap<>(8);

    private ExecutorPool(int threadTotal, String nameFormat){
        ThreadFactory factory = new ThreadFactoryBuilder().setNameFormat(nameFormat).build();
        this.service = new ThreadPoolExecutor(5, 50, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<>(1024), factory, new ThreadPoolExecutor.AbortPolicy());
        //信号量
        this.semaphore = new Semaphore(threadTotal);
    }

    public void doWork(WorkAction action) {
        try {
            service.execute(() -> {
                try {
                    semaphore.acquire();
                    action.execute();
                } catch (InterruptedException e) {
                    this.shutDown();
                }
                semaphore.release();
            });
        }catch (BasicBoundedBufferException e){
            this.shutDown();
        }
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

    public static ExecutorPool getInstance(int threadTotal, String threadName){
        ExecutorPool executorPool = singletonCache.get(threadName);
        if(Objects.isNull(executorPool)){
            synchronized (ExecutorPool.class){
                ExecutorPool newExecutorPool = new ExecutorPool(threadTotal, threadName.concat("-Thread-%d"));
                singletonCache.put(threadName, newExecutorPool);
                return newExecutorPool;
            }
        }
        return executorPool;
    }

}
