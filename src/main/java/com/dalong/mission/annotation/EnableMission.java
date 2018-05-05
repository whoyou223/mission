package com.dalong.mission.annotation;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 开启任务
 *
 * @author dalong
 * @summary 开启任务
 * @since 2018-05-02 下午6:09
 **/
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(MissionImportSelector.class)
public @interface EnableMission {
}
