package com.dalong.mission.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

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
        return new ThreadPoolExecutor(50, 400,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>());
    }

    @Bean
    public ThreadPoolExecutor linkedExecutor(){
        int n = Runtime.getRuntime().availableProcessors();
        int pooSize = 15 * n;
        return new ThreadPoolExecutor(pooSize, 3 * pooSize, 60L,
                TimeUnit.SECONDS, new LinkedBlockingDeque<>(2048));
    }
}
