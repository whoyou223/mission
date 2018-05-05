package com.dalong.mission.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 分布式任务实体
 *
 * @author dalong
 * @summary
 * @since 2018-04-28 上午11:04
 **/
@Data
public class MissionEntity implements Serializable{

    /**
     * 任务id，全局唯一，除非redis挂了
     */
    private int id;

    /**
     * 执行任务的类名
     */
    private String className;

    /**
     * 执行任务的方法名
     */
    private String methodName;

    /**
     * 任务方法是否需要参数
     */
    private boolean parameter;

    /**
     * 任务的参数类型
     */
    private String parameterClassName;

    /**
     * 任务的参数值jason
     */
    private String parameterJason;

    /**
     * 本次任务创建时间
     */
    private long ctime;

    /**
     * 本次任务超时时间；配合abdicable使用
     * </br>
     * 为防止服务器节点时间不一致，超时时间为下次任务执行前1分钟
     */
    private long timeOut;

    /**
     * 是否可丢弃；默认否；
     * </br>
     * 任务超时，若可丢弃则直接丢弃本次任务，不丢弃则执行，执行失败将放入重试队列一直重试
     */
    private boolean abdicable;

    /**
     * 任务执行失败后重试次数，默认1；
     * </br>
     * 0:不重试，直接丢弃;
     */
    private int retry;

}
