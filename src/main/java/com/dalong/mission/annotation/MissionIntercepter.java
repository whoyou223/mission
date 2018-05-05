package com.dalong.mission.annotation;

import com.dalong.mission.config.MissionProperties;
import com.dalong.mission.service.MissionProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * @author dalong
 * @summary
 * @since 2018-04-28 下午2:16
 **/
@Aspect
public class MissionIntercepter {
    private final static Logger logger = LoggerFactory.getLogger(MissionIntercepter.class);

    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    private MissionProperties missionProperties;

    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MissionProducer missionProducer;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Pointcut("@annotation(com.dalong.mission.annotation.Mission)")
    private void missionPointCut() {
    }

    @Around("missionPointCut()")
    public void missionAround(ProceedingJoinPoint joinPoint) {

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();

        Scheduled scheduled = methodSignature.getMethod().getAnnotation(Scheduled.class);
        Mission mission = methodSignature.getMethod().getAnnotation(Mission.class);

        // cron
        String cron = scheduled.cron();

        // 任务类
        Class<?> clazz = mission.clazz();

        // 任务方法名
        String methodName = mission.method();

        // 是否有参数
        boolean parameter = mission.parameter();

        // 任务能否丢弃
        boolean abdicable = mission.abdicable();

        // 失败重试次数
        int retry = mission.retry();

        logger.info("拦截到定时任务：" + clazz.getSimpleName() + "." + methodName);

        // 任务方法参数类型
        Class<?> parameterClazz = null;

        // 注释方法的返回值
        Object result = null;

        Assert.isTrue(!StringUtils.isEmpty(methodName), "Mission解析异常：methodName 不能为空");

        // 如果有参数，校验参数是否正确
        if (parameter) {
            Class returnType = methodSignature.getMethod().getReturnType();
            // 返回值类型不能为void
            Assert.isTrue(!returnType.getName().equals(Void.TYPE.getName()), "Mission解析异常：设置有参数时，注释方法返回值类型不能为void");

            // 返回值类型应该和任务方法的参数类型相同
            Method method = null;
            try {
                method = clazz.getMethod(methodName, returnType);
            } catch (NoSuchMethodException e) {
                Assert.isTrue(false, "Mission解析异常：未找到指定参数类型的方法" + methodName);
                return;
            }

            // 匹配方法参数数量和参数类型
            Class[] parameterTypes = method.getParameterTypes();
            Assert.isTrue(parameterTypes.length == 1, "Mission解析异常：Sorry！目前只支持一个参数..." + clazz.getSimpleName() + "类的" + methodName + "方法");
            parameterClazz = parameterTypes[0];

            // 获取结果作为任务参数
            try {
                result = joinPoint.proceed();
            } catch (Throwable e) {
                logger.error("Mission解析异常：任务参数生成失败。" + e.getMessage());
                return;
            }
        }

        // 加锁，生产任务
        missionProducer.produce(clazz, methodName, parameter, parameterClazz, result, cron, abdicable, retry);

    }
}
