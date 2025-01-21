package com.studyolle.infra.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int processors = Runtime.getRuntime().availableProcessors(); // 현재 시스템의 프로세스 개수
        log.info("processor count: {}", processors);
        executor.setCorePoolSize(processors); // 튜브 수
        executor.setMaxPoolSize(processors * 2);  // 줄새움50까지 근데 51이되면? 튜브수 * 2만큼은 더 만들어줌
        executor.setQueueCapacity(50); // 튜브 수가 꽉차면 줄새움
        executor.setKeepAliveSeconds(60); // 더만든 큐브 생명력
        executor.setThreadNamePrefix("AsyncExecutor-");
        executor.initialize();
        return executor;
    }
}
