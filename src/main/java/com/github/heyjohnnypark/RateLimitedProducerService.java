package com.github.heyjohnnypark;

import com.google.common.util.concurrent.RateLimiter;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;
import javax.annotation.PostConstruct;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class RateLimitedProducerService {

  private static final Logger LOG = LoggerFactory.getLogger(App.class);

  @Value("${kafka.topic}")
  private String topic;

  @Value("${kafka.messageSize:1024}")
  private int messageSize;

  @Value("${kafka.numMessages:1000}")
  private int numMessages;

  @Value("${kafka.ratePerSecond:100}")
  private int ratePerSecond;

  @Autowired
  private MetricMeter metricMeter;

  private RateLimiter rateLimiter;

  private byte[] message;

  @Autowired
  private KafkaTemplate<String, byte[]> template;

  @PostConstruct
  public void postConstruct() {
    rateLimiter = RateLimiter.create(ratePerSecond);
    generateMessage();
  }

  @Async
  public CompletableFuture<Long> produceMessages() {
    LOG.info("Start producing {} messages (size={} bytes) at a rate of {}/second",
        numMessages,
        messageSize,
        ratePerSecond);
    long count = IntStream
        .range(0, numMessages)
        .mapToObj(this::createProducerRecord)
        .peek(this::registerRecord)
        .peek(this::sendRecord)
        .peek(this::rateLimit)
        .count();
    LOG.info("Produced {} messages", count);
    return CompletableFuture.completedFuture(count);
  }

  private void generateMessage() {
    message = new byte[messageSize];
    new Random().nextBytes(message);
    LOG.info("Generated message with {} bytes", message.length);
  }

  private ProducerRecord<String, byte[]> createProducerRecord(int i) {
    return new ProducerRecord(topic, UUID.randomUUID().toString(), message);
  }

  private ProducerRecord rateLimit(ProducerRecord record) {
    rateLimiter.acquire();
    return record;
  }

  private ProducerRecord sendRecord(ProducerRecord record) {
    template.send(record);
    return record;
  }

  private ProducerRecord registerRecord(ProducerRecord<String, byte[]> record) {
    metricMeter.onMessageOut(record.key());
    return record;
  }

}
