package com.dalong.mission.service;

import com.dalong.mission.config.MissionProperties;
import com.dalong.mission.entity.MissionEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 任务管理:黑名单管理，三个队列中的任务查看
 *
 * @author dalong
 * @summary 任务管理
 * @since 2018-05-03 下午5:49
 **/
@Component
public class MissionManager {

    private Logger logger = LoggerFactory.getLogger(MissionManager.class);

    @Autowired
    private MissionProperties missionProperties;

    @Autowired
    private RedisTemplate redisTemplate;

    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 查询每个队列里的任务
     * @param queue 队列名，queue、retry、fail
     * @return 任务list
     */
    public List<MissionEntity> list(String queue) {
        String queueName;
        switch (queue) {
            case "queue":
                queueName = missionProperties.getMissionQueue();
                break;
            case "retry":
                queueName = missionProperties.getRetryQueue();
                break;
            case "fail":
                queueName = missionProperties.getFailQueue();
                break;
            default:
                queueName = null;
        }
        if (Objects.nonNull(queueName)) {
            List<String> list = this.redisTemplate.opsForList().range(queueName, 0, -1);
            return list.stream()
                    .map(jason -> {
                        try {
                            return this.objectMapper.readValue(jason, MissionEntity.class);
                        } catch (Exception e) {
                            logger.error("反序列化任务实体出错：" + e.getMessage());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }




}
