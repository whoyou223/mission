package com.dalong.mission.util;

import org.springframework.scheduling.support.CronSequenceGenerator;

import java.util.Date;

/**
 * cron表达式解析
 *
 * @author dalong
 * @summary
 * @since 2018-05-03 上午10:13
 **/
public class CronParser {

    public static long nextTime(String cron){
        final CronSequenceGenerator generator = new CronSequenceGenerator(cron);
        return generator.next(new Date()).getTime();
    }
}
