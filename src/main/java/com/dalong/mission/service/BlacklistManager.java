package com.dalong.mission.service;

import com.dalong.mission.config.MissionProperties;
import com.dalong.mission.entity.BlacklistEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * 黑名单管理
 *
 * @author dalong
 * @summary
 * @since 2018-05-04 上午10:24
 **/
@Component
public class BlacklistManager {
    private Logger logger = LoggerFactory.getLogger(BlacklistManager.class);

    @Autowired
    private MissionProperties missionProperties;

    @Autowired
    private RedisTemplate redisTemplate;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Set<BlacklistEntity> blacklist = new HashSet<>();

    // todo 暂不提供黑名单保存功能，服务器重启后清空
//    @PostConstruct
//    public void init() {
//        List<String> list = redisTemplate.opsForList().range(missionProperties.getBlacklist(), 0, -1);
//        if (list.size() > 0) {
//            blacklist.addAll(
//                    list.stream()
//                            .map(jason -> {
//                                try {
//                                    return this.objectMapper.readValue(jason, BlacklistEntity.class);
//                                } catch (Exception e) {
//                                    logger.error("反序列化黑名单实体出错：" + e.getMessage());
//                                    return null;
//                                }
//                            })
//                            .filter(Objects::nonNull)
//                            .collect(Collectors.toList())
//            );
//        }
//        logger.info("初始化本地黑名单完成...");
//    }


    public void handleMessage(String message) throws IOException {
        logger.info("收到通知：" + message);
        BlacklistEntity entity = objectMapper.readValue(message, BlacklistEntity.class);
        
        if (entity.getType() == 1){
            this.blacklist.add(entity);
        }else if (entity.getType() == 2){
            this.blacklist.remove(entity);
        }else {
            logger.info("未知黑名单操作类型：type=" + entity.getType());
        }
    }

    /**
     * 是否黑名单
     *
     * @param className 简单类名
     * @param methodName 方法名
     * @return 是-true，否-false
     */
    public boolean isBlack(String className, String methodName){
        if (className.contains(".")){
            className = className.substring(className.lastIndexOf(".") + 1);
        }

        if (StringUtils.isEmpty(className) || StringUtils.isEmpty(methodName)){
            return false;
        }
        BlacklistEntity entity = new BlacklistEntity();
        entity.setClassName(className);
        entity.setMethodName(methodName);
        return this.blacklist.contains(entity);
    }



    public void addBlacklist(String className, String methodName) throws Exception{
        Assert.isTrue(!StringUtils.isEmpty(className) && !StringUtils.isEmpty(methodName), "参数不能为空");
        BlacklistEntity entity = new BlacklistEntity();
        entity.setClassName(className);
        entity.setMethodName(methodName);
        entity.setType(1);
        this.redisTemplate.convertAndSend(missionProperties.getChannel(), objectMapper.writeValueAsString(entity));
    }

    public void deleteBlackList(String className, String methodName) throws Exception{
        Assert.isTrue(!StringUtils.isEmpty(className) && !StringUtils.isEmpty(methodName), "参数不能为空");
        BlacklistEntity entity = new BlacklistEntity();
        entity.setClassName(className);
        entity.setMethodName(methodName);
        entity.setType(2);
        this.redisTemplate.convertAndSend(missionProperties.getChannel(), objectMapper.writeValueAsString(entity));
    }

    public Set<BlacklistEntity> getBlacklist(){
        return this.blacklist;
    }



}
