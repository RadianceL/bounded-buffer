package com.el.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author eddie
 * @createTime 2019-05-26
 * @description 执行的任务包装
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExecuteJob {

    private String name;

    private Object value;

}
