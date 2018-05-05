package com.dalong.mission.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 分布式任务配置信息
 *
 * @author dalong
 * @summary
 * @since 2018-04-28 上午11:33
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "mission")
public class MissionProperties {

    /**
     * 默认任务超时和下周期任务执行的时间差：1秒
     */
    public final static int DEFAULT_TIME_DIFF = 5;

    /**
     * 默认任务加锁的值
     */
    public final static String DEFAULT_LOCK_VALUE = "-1";


    /**
     * redis中的任务队列key
     */
    private String missionQueue;

    /**
     * redis中的任务重试队列key
     */
    private String retryQueue;

    /**
     * redis中的失败任务队列
     */
    private String failQueue;

    /**
     * redis中用于生成任务序号的key
     */
    private String indexKey;

    /**
     * redis中黑名单管理的key
     */
    private String blacklist;

    /**
     * 用于黑名单订阅的channel
     */
    private String channel;

    /**
     * 任务消费的间隔时间：秒数
     */
    private int period;

    /**
     * 是否开启任务执行日志
     */
    private boolean logEnable;

    /**
     * redis中任务执行日志的队列
     */
    private String logQueue;

    /**
     * 日志最大数量限制
     */
    private int maxLogNum;


}
