package it.gov.pagopa.mypay2pu.extractor.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@EnableConfigurationProperties(ExtractorAsyncProperties.class)
public class AsyncConfig {

  @Bean("extractorTaskExecutor")
  public TaskExecutor extractorTaskExecutor(ExtractorAsyncProperties extractorAsyncProperties) {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(extractorAsyncProperties.corePoolSize());
    executor.setMaxPoolSize(extractorAsyncProperties.maxPoolSize());
    executor.setQueueCapacity(extractorAsyncProperties.queueCapacity());
    executor.setThreadNamePrefix("extractor-");
    executor.initialize();
    return executor;
  }
}
