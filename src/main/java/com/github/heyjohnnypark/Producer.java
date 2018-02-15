package com.github.heyjohnnypark;

import java.util.concurrent.CompletableFuture;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class Producer implements ApplicationContextAware {

  private static final Logger LOG = LoggerFactory.getLogger(App.class);

  private ApplicationContext ctx;

  @Autowired
  private MetricMeter meter;

  @Autowired
  private RateLimitedProducerService service;

  @PostConstruct
  public void postConstruct() {
    service.produceMessages()
        .thenAccept(numMessages -> meter.report());
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.ctx = applicationContext;
  }
}
