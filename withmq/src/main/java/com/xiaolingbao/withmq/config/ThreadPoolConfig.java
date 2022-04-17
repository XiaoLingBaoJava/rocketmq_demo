package com.xiaolingbao.withmq.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class ThreadPoolConfig {

    @Bean("threadPool")
    public ExecutorService executorService() {
        return new ThreadPoolExecutor( 4, 10, 3L,
                TimeUnit.SECONDS, new ArrayBlockingQueue<>(10), Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());

    }

}
