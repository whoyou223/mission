package com.dalong.mission.service;

import com.dalong.mission.config.MissionProperties;
import com.dalong.mission.entity.MissionEntity;
import com.dalong.mission.util.CronParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 任务生产者
 *
 * @author dalong
 * @summary
 * @since 2018-04-28 下午4:21
 **/
@Component
public class MissionProducer {
    private final static Logger logger = LoggerFactory.getLogger(MissionProducer.class);

    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    private MissionProperties missionProperties;

    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IndexGenerator indexGenerator;

    @Autowired
    private BlacklistManager blacklistManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void produce(Class<?> clazz, String methodName, boolean parameter, Class<?> parameterClazz, Object result, String cron, boolean abdicable, int retry) {
        Assert.isTrue(retry == -1 || retry >= 0, "Mission解析错误：参数错误 retry = " + retry);

        String parameterJason = "";

        // 序列化参数值
        if (parameter && Objects.nonNull(result)) {
            try {
                parameterJason = objectMapper.writeValueAsString(result);
            } catch (Exception e) {
                logger.error("Mission解析错误：序列化方法参数值出错," + e.getMessage());
                return;
            }
        }
        // model
        MissionEntity missionEntity = new MissionEntity();
        missionEntity.setClassName(clazz.getName());
        missionEntity.setMethodName(methodName);
        missionEntity.setParameter(parameter);
        missionEntity.setAbdicable(abdicable);
        missionEntity.setRetry(retry);
        if (parameter) {
            missionEntity.setParameterClassName(parameterClazz.getName());
            missionEntity.setParameterJason(parameterJason);
        }

        // 是否在黑名单
        if (blacklistManager.isBlack(clazz.getSimpleName(), methodName)){
            return;
        }

        // 解析cron得到下个周期的时间戳
        long nextTime = CronParser.nextTime(cron);
        long ctime = System.currentTimeMillis();
        long timeOut = nextTime - MissionProperties.DEFAULT_TIME_DIFF * 1000;
        missionEntity.setCtime(ctime);
        missionEntity.setTimeOut(timeOut);

        // 加锁
        String key = clazz.getName() + "." + methodName;
        try {
            // 本次任务加锁
            if (redisTemplate.opsForValue().setIfAbsent(key, MissionProperties.DEFAULT_LOCK_VALUE)) {
                // 设置任务生产锁超时时间
                redisTemplate.expire(key, (timeOut - ctime) / 1000, TimeUnit.SECONDS);
                // 生成id
                missionEntity.setId(indexGenerator.getIndex());
                // 将任务放入队列
                this.inQueue(missionProperties.getMissionQueue(), missionEntity);
            } else {
                // 加锁失败，表明任务已生产
                logger.info("Mission:此次任务已生产，跳过执行");
            }
        } catch (Exception e) {
            // 异常，无论是加锁、设置超时、放入队列，均删除key
            logger.error("Mission生产出错：" + e.getMessage());
            redisTemplate.delete(key);
        }
    }

    public void inQueue(String queueName, MissionEntity missionEntity) {
        // 序列化
        String value  ;
        try {
            value = objectMapper.writeValueAsString(missionEntity);
        } catch (JsonProcessingException e) {
            logger.error("入队失败，序列化出错：" + e.getMessage());
            throw new RuntimeException();
        }
        // 队尾入队
        redisTemplate.opsForList().rightPush(queueName, value);
        String className = missionEntity.getClassName();
        logger.info("入队成功：" + className.substring(className.lastIndexOf(".") + 1) + "." + missionEntity.getMethodName());
    }


}
