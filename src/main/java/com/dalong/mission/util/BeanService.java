package com.dalong.mission.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * spring容器
 *
 * @author dalong
 * @summary
 * @since 2018-05-05 下午12:18
 **/
public class BeanService {
    private static ApplicationContext delegateContext;

    public BeanService() {
    }

    static void setContext(ApplicationContext applicationContext) {
        delegateContext = applicationContext;
    }

    public static <T> T of(Class<T> cls) {
        return delegateContext.getBean(cls);
    }

    public static <T> T withName(String beanName, Class<T> cls) {
        return delegateContext.getBean(beanName, cls);
    }

    public static <T> Map<String, T> beansOfTypeIncludingAncestors(Class<T> clazz) {
        return BeanFactoryUtils.beansOfTypeIncludingAncestors(delegateContext, clazz);
    }

    @Configuration
    static class ServicesConfig implements ApplicationContextAware {
        ServicesConfig() {
        }

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            BeanService.delegateContext = applicationContext;
        }
    }
}
