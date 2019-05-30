package com.el.worker;

import com.el.exceptions.BoundedBufferThreadShutdownExcetption;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author eddie
 * @createTime 2019-05-30
 * @description 线程池工厂
 */
public class ExecutorPoolFactory {

    /**
     * 缓存
     */
    private static ConcurrentHashMap<String, ExecutorPool> singletonCache = new ConcurrentHashMap<>(8);

    /**
     * 当前有多少线程池在缓存中
     */
    private static AtomicInteger totalExecutorPool = new AtomicInteger(0);

    private void killExectorPool(ExecutorPool executorPool){
        totalExecutorPool.decrementAndGet();
        executorPool.shutDown();
        if (!singletonCache.remove(executorPool.getThreadName(), executorPool)){
            throw new BoundedBufferThreadShutdownExcetption("线程未成功关闭，缓存清理失败");
        }
    }

    /**
     * 获取总共有多少线程池
     * @return
     */
    public static int getTotalEcecutorPool() {
        return totalExecutorPool.get();
    }

    public static ExecutorPool getInstance(int threadTotal, String poolName) {
        ExecutorPool executorPool = singletonCache.get(poolName);
        if (Objects.isNull(executorPool)){
            executorPool = ExecutorPool.getInstance(threadTotal, poolName);
            singletonCache.put(poolName, executorPool);
        }
        return executorPool;
    }

}
