package com.dalong.mission.annotation;


import java.lang.annotation.*;

/**
 *
 * @author dalong
 * @version v1
 * @summary 分布式定时任务
 * @since 2018-04-28 10:28:14
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Mission {

    /**
     * 执行任务的类
     * @return java类
     */
    Class<?> clazz();

    /**
     * 执行任务的方法名
     *
     * @return 方法名
     */
    String method();

    /**
     * 是否有参数，默认无；
     * 如果有，则使用注解@Mission修饰的方法的返回值作为任务方法的参数，请匹配返回值类型和参数类型；
     * @return 是否有参数
     */
    boolean parameter() default false;

    /**
     * 任务是否可放弃；
     * </br>
     * 任务延迟(异常或等待其他任务)导致超时，如果可放弃，则直接丢弃本次任务；
     *
     * @return 是否可放弃
     */
    boolean abdicable() default true;

    /**
     * 任务执行失败后重试次数；
     * 任务失败-->重试-->重试完或者超时-->是否可丢弃
     * </br>
     * 0:不重试，直接丢弃;
     * -1:无限重试，直到超时
     *
     * @return 重试次数
     */
    int retry() default 0;


}
