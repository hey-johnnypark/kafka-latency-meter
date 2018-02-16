package com.github.heyjohnnypark;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MetricMeter {

  private static String ROUNDTRIP_LATENCY_TEMPLATE = "roundtrip-latency-topic[%s]";

  private Map<String, Long> records = new ConcurrentHashMap<>();

  private final MetricRegistry metrics = new MetricRegistry();

  private ConsoleReporter reporter;

  @Value("${kafka.topic}")
  private String topic;

  private String key;

  @PostConstruct
  private void init() {
    key = String.format(ROUNDTRIP_LATENCY_TEMPLATE, topic);
    reporter = ConsoleReporter
        .forRegistry(metrics)
        .convertRatesTo(TimeUnit.SECONDS)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
        .build();

  }

  public void onMessageOut(String msgId) {
    records.put(msgId, System.currentTimeMillis());
  }

  public void onMessageIn(String msgId) {
    Optional
        .ofNullable(records.get(msgId))
        .ifPresent(time -> {
          metrics
              .timer(key)
              .update(System.currentTimeMillis() - time, TimeUnit.MILLISECONDS);
        });
  }

  public void report() {
    reporter.report();
  }


}
