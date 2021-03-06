package com.github.heyjohnnypark;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class Consumer  {

  private static final Logger LOG = LoggerFactory.getLogger(App.class);

  @Autowired
  private MetricMeter metricMeter;

  @KafkaListener(topics = "${kafka.topic}")
  public void onMessage(ConsumerRecord<String, String> record) {
    metricMeter.onMessageIn(record.key());
  }

}

