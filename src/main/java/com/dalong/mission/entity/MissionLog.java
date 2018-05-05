package com.dalong.mission.entity;

import lombok.Data;

/**
 * @author dalong
 * @summary
 * @since 2018-05-02 下午3:59
 **/
@Data
public class MissionLog {

    /**
     * log生成时间
     */
    private long time;
    /**
     * 任务执行时间
     */
    private String etime;

    /**
     * 任务创建时间
     */
    private String ctime;

    /**
     * 执行任务的服务器ip
     */
    private String ip;

    /**
     * 任务类名
     */
    private String className;

    /**
     * 任务方法名
     */
    private String methodName;

    /**
     * 耗时，毫秒数
     */
    private int cost;

    /**
     * 操作类型
     */
    private String exeResult;
}
