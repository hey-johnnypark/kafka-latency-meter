package com.github.heyjohnnypark;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class MetricMeter {

  private static String NUM_MESSAGES_RECEIVED = "num-messages-received";

  private static String MESSAGES_LATENCY = "messages-latency";

  private Map<String, Long> records = new ConcurrentHashMap<>();

  private final MetricRegistry metrics = new MetricRegistry();

  private ConsoleReporter reporter;

  @PostConstruct
  private void init() {
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
              .timer(MESSAGES_LATENCY)
              .update(System.currentTimeMillis() - time, TimeUnit.MILLISECONDS);
        });
  }

  public void report() {
    reporter.report();
  }


}
