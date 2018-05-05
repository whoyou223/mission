package com.dalong.mission.annotation;

import com.dalong.mission.config.BlacklistConfig;
import com.dalong.mission.config.MissionProperties;
import com.dalong.mission.controller.MissionController;
import com.dalong.mission.service.*;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 导入
 *
 * @author dalong
 * @summary
 * @since 2018-05-02 下午6:10
 **/
public class MissionImportSelector implements ImportSelector, EnvironmentAware {

    private static final String MISSION_ENABLE = "mission.enable";

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        //配置禁用
        if (!this.environment.getProperty(MISSION_ENABLE, Boolean.class, Boolean.TRUE)) {
            return new String[]{};
        }


        return new String[]{MissionIntercepter.class.getName(), MissionConsumer.class.getName(),MissionProducer.class.getName(),
        MissionLogService.class.getName(), MissionProperties.class.getName(), MissionManager.class.getName(),
                MissionController.class.getName(), IndexGenerator.class.getName(), BlacklistConfig.class.getName(), BlacklistManager.class.getName()};
    }
}
