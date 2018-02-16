# Kafka Latency Meter

> Measures the latency of a Kafka topic

## Getting started

```bash
$ git clone https://github.com/hey-johnnypark/kafka-latency-meter.git && \
  cd kafka-latency-meter && \
  mvn clean install && \
  java -jar target/kafka-latency-meter.jar --kafka.topic=foobar
```

## Features

* Measures roundtrip latency for a single kafka topic
* Latency test can be parametrized 

## Configuration

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      auto-offset-reset: latest
      group-id: com.github.hey-johnnypark-kafka-latency-meter
      enable-auto-commit: true
      value-deserializer: org.apache.kafka.common.serialization.ByteArrayDeserializer
    producer:
      value-serializer: org.apache.kafka.common.serialization.ByteArraySerializer

kafka:
  topic: in # Topic to measure the latency for
  messageSize: 1024 # Size of test messages in bytes
  numMessages: 1000 # Number of messages 
  ratePerSecond: 200 # Max produce rate
```


