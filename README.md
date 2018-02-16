# Kafka Latency Meter

> Measures the latency of a Kafka topic

## Getting started

<b>Optional</b>: Start Kafka in Docker
```bash
docker run --name broker --rm -p 2181:2181 -p 9092:9092 -e ADVERTISED_HOST=127.0.0.1 johnnypark/kafka-zookeeper
```

Checkout, build and run kafka-latency-meter
```bash
$ git clone https://github.com/hey-johnnypark/kafka-latency-meter.git && \
  cd kafka-latency-meter && \
  mvn clean install && \
  java -jar target/kafka-latency-meter.jar --kafka.topic=foobar

-- Timers ----------------------------------------------------------------------
roundtrip-latency-topic[foobar]
             count = 10000
         mean rate = 512.32 calls/second
     1-minute rate = 541.48 calls/second
     5-minute rate = 547.39 calls/second
    15-minute rate = 548.46 calls/second
               min = 0.00 milliseconds
               max = 167.00 milliseconds
              mean = 3.91 milliseconds
            stddev = 19.71 milliseconds
            median = 1.00 milliseconds
              75% <= 2.00 milliseconds
              95% <= 2.00 milliseconds
              98% <= 3.00 milliseconds
              99% <= 152.00 milliseconds
            99.9% <= 166.00 milliseconds
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
  topic: in           # Topic to measure the latency for
  messageSize: 1024   # Size of test messages in bytes
  numMessages: 1000   # Number of messages 
  ratePerSecond: 200  # Max produce rate
```


