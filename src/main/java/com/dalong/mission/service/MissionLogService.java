package com.dalong.mission.service;

import com.dalong.mission.config.MissionProperties;
import com.dalong.mission.entity.MissionEntity;
import com.dalong.mission.entity.MissionLog;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author dalong
 * @summary 任务日志
 * @since 2018-05-02 下午4:02
 **/
@Component
public class MissionLogService {
    private Logger logger = LoggerFactory.getLogger(MissionLogService.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MissionProperties missionProperties;

    private ObjectMapper objectMapper = new ObjectMapper();


    public void info(MissionEntity missionEntity, int cost, String exeResult) {
        logger.info("记录执行任务日志");
        if (Objects.nonNull(missionEntity)) {
            String ip = "";
            try {
                ip = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                logger.error("获取服务器ip地址出错");
            }
            String className = missionEntity.getClassName();
            className = className.substring(className.lastIndexOf(".") + 1);
            // 添加日志

            MissionLog log = new MissionLog();
            log.setTime(System.currentTimeMillis());
            log.setEtime(LocalDateTime.now().toString());
            log.setCtime(LocalDateTime.ofInstant(Instant.ofEpochMilli(missionEntity.getCtime()), ZoneId.systemDefault()).toString());
            log.setIp(ip);
            log.setClassName(className);
            log.setMethodName(missionEntity.getMethodName());
            log.setCost(cost);
            log.setExeResult(exeResult);

            // 放哪好呢？？？

            String logValue;
            try {
                logValue = objectMapper.writeValueAsString(log);
            } catch (Exception e) {
                logger.error("mission任务日志序列化失败：" + e.getMessage());
                return;
            }

            this.redisTemplate.opsForList().rightPush(missionProperties.getLogQueue(), logValue);
            logger.info("添加日志成功，max=" + missionProperties.getMaxLogNum());
            if (this.redisTemplate.opsForList().size(missionProperties.getLogQueue()) > missionProperties.getMaxLogNum()) {
                this.redisTemplate.opsForList().leftPop(missionProperties.getLogQueue());
            }
        }
    }

    public List<MissionLog> getLog(String className, String methodName, String exeResult) {

        int totalCount = this.redisTemplate.opsForList().size(missionProperties.getLogQueue()).intValue();

        if (totalCount == 0) {
            return Collections.emptyList();
        }

        List<String> logList = this.redisTemplate.opsForList().range(missionProperties.getLogQueue(), 0, -1);

        List<MissionLog> result = logList.stream()
                .map(jason -> {
                    try {
                        return objectMapper.readValue(jason, MissionLog.class);
                    } catch (Exception e) {
                        logger.error("反序列化日志出错：" + e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (!StringUtils.isEmpty(className)) {
            result = result.stream()
                    .filter(missionLog -> missionLog.getClassName().equals(className))
                    .collect(Collectors.toList());
        }
        if (!StringUtils.isEmpty(methodName)) {
            result = result.stream()
                    .filter(missionLog -> missionLog.getClassName().equals(methodName))
                    .collect(Collectors.toList());
        }
        if (!StringUtils.isEmpty(exeResult)) {
            result = result.stream()
                    .filter(missionLog -> missionLog.getClassName().equals(exeResult))
                    .collect(Collectors.toList());
        }
        // 按照执行时间倒序排列
        result.sort(Comparator.comparing(MissionLog::getTime).reversed());

        return result;
    }

    public enum ExeResult {
        执行完成("执行完成"), 失败重试("失败重试"), 失败丢弃("失败丢弃"), 失败入队("失败入队");

        private String value;

        private ExeResult(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

}
