package com.dalong.mission.service;

import com.dalong.mission.config.MissionProperties;
import com.dalong.mission.entity.MissionEntity;
import com.dalong.mission.util.BeanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 任务消费者
 *
 * @author dalong
 * @summary
 * @since 2018-05-02 下午2:08
 **/
@Component
public class MissionConsumer {
    private final static Logger logger = LoggerFactory.getLogger(MissionConsumer.class);

    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    private MissionProperties missionProperties;

    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MissionLogService missionLogService;

    @Autowired
    private MissionProducer missionProducer;
    @Autowired
    private BlacklistManager blacklistManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

    @PostConstruct
    public void init() {
        int random = 60 * 2 + (int) (Math.random() * 60);
        executor.scheduleWithFixedDelay(this::work, random, missionProperties.getPeriod(), TimeUnit.SECONDS);
        logger.info("启动mission消费，" + random + "秒后开始工作，工作间隔：" + missionProperties.getPeriod() + "秒...");
        if (missionProperties.isLogEnable()) {
            logger.info("mission:开启任务执行日志记录，日志数量限制：" + missionProperties.getMaxLogNum() + "日志队列：" + missionProperties.getLogQueue());
        }
    }

    public void work() {
        // 任务队列里取一个
        MissionEntity missionEntity = this.getMission(missionProperties.getMissionQueue());
        this.consume(missionEntity, false);

        // 重试队列取一个
        missionEntity = this.getMission(missionProperties.getRetryQueue());
        this.consume(missionEntity, true);
    }

    private void consume(MissionEntity missionEntity, boolean reConsume) {

        if (Objects.isNull(missionEntity)) {
            return;
        }


        // 任务是否超时
        if (missionEntity.getTimeOut() < System.currentTimeMillis() && missionEntity.isAbdicable()) {
            logger.info("任务超时，直接丢弃");
            // 丢弃
            return;
        }
        if (missionEntity.getTimeOut() < System.currentTimeMillis() && !missionEntity.isAbdicable()) {
            logger.info("任务超时，放入失败队列");
            missionProducer.inQueue(missionProperties.getFailQueue(), missionEntity);
            return;
        }
        if (blacklistManager.isBlack(missionEntity.getClassName(), missionEntity.getMethodName())){
            logger.info("任务在黑名单中，直接丢弃");
            return;
        }

        // 执行任务
        long start = System.currentTimeMillis();
        long cost = 0;
        String result;
        try {
            this.execute(missionEntity);
            cost = System.currentTimeMillis() - start;
            result = MissionLogService.ExeResult.执行完成.getValue();
        } catch (Exception e) {
            // 失败
            logger.info("执行任务失败--任务：" + missionEntity.toString() + ",原因： " + e.getMessage());

            // 重试次数-1
            if (reConsume) {
                missionEntity.setRetry(missionEntity.getRetry() - 1);
            }

            if (missionEntity.getRetry() > 0) {
                // 继续重试
                missionProducer.inQueue(missionProperties.getRetryQueue(), missionEntity);
                result = MissionLogService.ExeResult.失败重试.getValue();
            } else if (!missionEntity.isAbdicable()) {
                // 放入失败队列
                missionProducer.inQueue(missionProperties.getFailQueue(), missionEntity);
                result = MissionLogService.ExeResult.失败入队.getValue();
            } else {
                // 丢弃
                logger.info("执行任务失败，直接丢弃");
                result = MissionLogService.ExeResult.失败丢弃.getValue();
            }
        }

        // 日志
        if (missionProperties.isLogEnable()){
            missionLogService.info(missionEntity, (int)cost, result);
        }
    }

    private void execute(MissionEntity missionEntity) throws Exception {
        if (Objects.nonNull(missionEntity)) {
            Class clazz = Class.forName(missionEntity.getClassName());
            String methodName = missionEntity.getMethodName();
            Object service = BeanService.of(clazz);

            Method method;
            Object parameter = null;
            Class parameterClazz = null;
            if (missionEntity.isParameter()) {
                // 参数类型
                parameterClazz = Class.forName(missionEntity.getParameterClassName());
                // 参数值
                String parameterJason = missionEntity.getParameterJason();
                parameter = this.objectMapper.readValue(parameterJason, parameterClazz);
            }
            // 获取方法
            method = clazz.getMethod(methodName, parameterClazz);
            // 执行方法
            method.invoke(service, parameter);
            logger.info("任务执行成功：" + clazz.getSimpleName() + "." + methodName);
        }
    }

    private MissionEntity getMission(String queueName) {
        // 从队列里获取任务
        String missionJson = (String) this.redisTemplate.opsForList().leftPop(queueName);
        // 反序列化
        if (!StringUtils.isEmpty(missionJson)) {
            MissionEntity missionEntity;
            try {
                missionEntity = this.objectMapper.readValue(missionJson, MissionEntity.class);
                return missionEntity;
            } catch (Exception e) {
                logger.error("反序列化任务失败" + e.getMessage());
                return null;
            }
        }
        return null;
    }


}
