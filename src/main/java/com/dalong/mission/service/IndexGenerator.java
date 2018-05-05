package com.dalong.mission.service;

import com.dalong.mission.config.MissionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 序号生成器
 *
 * @author dalong
 * @summary
 * @since 2018-05-04 上午10:05
 **/
@Component
public class IndexGenerator {

    private Logger logger = LoggerFactory.getLogger(IndexGenerator.class);

    @Autowired
    private MissionProperties missionProperties;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostConstruct
    public void init(){
        if (redisTemplate.opsForValue().setIfAbsent(missionProperties.getIndexKey(), 1)){
            logger.info("初始化任务序号...");
        }
    }

    public int getIndex(){
        return redisTemplate.opsForValue().increment(missionProperties.getIndexKey(), 1).intValue();
    }
}
