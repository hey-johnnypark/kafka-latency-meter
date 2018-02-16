package com.github.heyjohnnypark;

import javax.annotation.PostConstruct;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class App implements ApplicationContextAware {

  @Autowired
  private MetricMeter meter;

  @Autowired
  private RateLimitedProducerService producer;

  private ApplicationContext ctx;

  public static void main(String[] args) {
    SpringApplication.run(App.class, args);
  }

  @PostConstruct
  private void initLatencyTest() {
    producer
        .produceMessages()
        .thenAccept(numMessages -> meter.report())
        .thenRun(() -> ((ConfigurableApplicationContext) ctx).close());
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.ctx = applicationContext;
  }
}