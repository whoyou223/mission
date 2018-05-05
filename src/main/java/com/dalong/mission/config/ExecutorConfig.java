package com.dalong.mission.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池
 *
 * @author dalong
 * @summary 线程池
 * @since 2017-12-06 上午10:28
 **/
@Configuration
public class ExecutorConfig {

    @Bean
    public ExecutorService executorService(){
        return new ThreadPoolExecutor(50, 300,
                30L, TimeUnit.SECONDS,
                new SynchronousQueue<>());
    }
}
