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
  public static final int NUM_STEPS = 20;

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

  private ProgressReporter reporter;

  @PostConstruct
  public void postConstruct() {
    initRateLimiter();
    initMockMessage();
    initProgressLogger();
  }

  private void initProgressLogger() {
    reporter = new ProgressReporter(numMessages);
  }

  private void initRateLimiter() {
    rateLimiter = RateLimiter.create(ratePerSecond);
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
        .peek(reporter::progress)
        .peek(this::rateLimit)
        .count();
    LOG.info("Done producing {} messages", count);
    return CompletableFuture.completedFuture(count);
  }

  private void initMockMessage() {
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

  private class ProgressReporter {

    private int currentProgress = 0;

    private final int end;

    private final int step;

    private ProgressReporter(int end) {
      this.end = end;
      step = end / NUM_STEPS;
    }

    private void progress(ProducerRecord record) {
      if (++currentProgress % step == 0) {
        LOG.info("Produced {}/{} messages ({}%)",
            currentProgress,
            end,
            (int)((float) currentProgress / (float) end * 100));
      }
    }

  }

}
