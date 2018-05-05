package com.dalong.mission.controller;

import com.dalong.mission.entity.BlacklistEntity;
import com.dalong.mission.entity.MissionEntity;
import com.dalong.mission.entity.MissionLog;
import com.dalong.mission.service.BlacklistManager;
import com.dalong.mission.service.MissionLogService;
import com.dalong.mission.service.MissionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/**
 * 接口
 *
 * @author dalong
 * @summary
 * @since 2018-05-03 下午6:47
 **/
@RestController
@RequestMapping(value = "/mission")
public class MissionController {

    @Autowired
    private MissionLogService missionLogService;
    @Autowired
    private MissionManager missionManager;
    @Autowired
    private BlacklistManager blacklistManager;

    /**
     * 查询任务执行日志，可通过类名、方法名、执行结果过滤
     *
     * @param className 简单类名，注意大小写
     * @param methodName 方法名，注意大小写
     * @param exeResult 执行结果：执行完成、失败重试、失败丢弃、失败入队
     * @return list
     */
    @RequestMapping(value = "/log", method = RequestMethod.GET)
    public List<MissionLog> getLog(@RequestParam(value = "className", required = false) String className,
                                   @RequestParam(value = "methodName", required = false) String methodName,
                                   @RequestParam(value = "exeResult", required = false) String exeResult){
        return missionLogService.getLog(className, methodName, exeResult);
    }

    /**
     * 实时查询各队列中任务列表
     *
     * @param queue 队列名，默认：queue，可选：retry-重试队列，fail-失败不丢弃队列
     * @return list
     */
    @RequestMapping(value = "/mission", method = RequestMethod.GET)
    public List<MissionEntity> getMission(@RequestParam(value = "queue", defaultValue = "queue") String queue){
        return missionManager.list(queue);
    }

    /**
     * 添加黑名单
     * @param className 类名
     * @param methodName 方法名
     * @return ok
     */
    @RequestMapping(value = "/blacklist/add", method = RequestMethod.GET)
    public String addBlacklist(@RequestParam(value = "className") String className, @RequestParam(value = "methodName") String methodName){
        try {
            blacklistManager.addBlacklist(className, methodName);
        } catch (Exception e) {
            return e.getMessage();
        }
        return "ok";
    }

    @RequestMapping(value = "/blacklist/delete", method = RequestMethod.GET)
    public String deleteBlackList(@RequestParam(value = "className") String className, @RequestParam(value = "methodName") String methodName){
        try {
            blacklistManager.deleteBlackList(className, methodName);
        } catch (Exception e) {
            return e.getMessage();
        }
        return "ok";
    }

    @RequestMapping(value = "/blacklist", method = RequestMethod.GET)
    public Set<BlacklistEntity> getBlacklist(){
        return blacklistManager.getBlacklist();
    }
}
