package com.el.worker;

/**
 * @author eddie
 * @createTime 2019-01-18
 * @description 工作内容
 */
@FunctionalInterface
public interface WorkAction {
    /**
     * 执行工作
     * @throws InterruptedException
     */
    void execute() throws InterruptedException;
}
