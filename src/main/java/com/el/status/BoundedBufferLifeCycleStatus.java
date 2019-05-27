package com.el.status;

/**
 * @author eddie
 * @createTime 2019-05-26
 * @description 生命周期状态
 */
public enum BoundedBufferLifeCycleStatus {

    /**
     * 开始 - 准备中
     */
    STARTING("准备中", 1),
    /**
     * 运行中
     */
    RUNNING("运行中", 2),
    /**
     * 暂停 - 可重启
     */
    SUSPEND("暂停", 0),
    /**
     * 停止中 - 不可恢复
     */
    SHUTDOWN("停止中", 3),
    /**
     * 已停止 - 不可恢复
     */
    STOPED("已停止", 4);

    /**
     * 标识描述
     */
    private String desc;

    /**
     * 标识编码
     */
    private int code;

    BoundedBufferLifeCycleStatus(String desc, int code){
        this.desc = desc;
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public int getCode() {
        return code;
    }

    /**
     * 标识编码获取标识描述
     *
     * @param code  标识编码
     * @return      标识描述
     */
    public static String getCode(int code) {
        for (BoundedBufferLifeCycleStatus e : values()) {
            if (e.getCode() == code) {
                return e.getDesc();
            }
        }
        return null;
    }
}
