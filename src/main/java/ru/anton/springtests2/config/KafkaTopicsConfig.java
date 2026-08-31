package ru.anton.springtests2.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class KafkaTopicsConfig {

    @Value("${kafka.consumer.delivery-created-topic}")
    private String deliveryCreatedTopic;

    @Value("${kafka.topics.partitions:1}")
    private int partitions;

    @Value("${kafka.topics.replication-factor:1}")
    private short replicationFactor;

    @Value("${kafka.topics.retry.attempts}")
    private int retryAttempts;

    @Value("${kafka.topics.retry.backoff-delay-ms}")
    private long retryBackoffDelayMs;

    @Value("${kafka.topics.retry.backoff-multiplier}")
    private double retryBackoffMultiplier;

    @Value("${kafka.topics.retry.backoff-max-delay-ms}")
    private long retryBackoffMaxDelayMs;

    @Value("${kafka.topics.dlt-suffix:.DLQ}")
    private String dltSuffix;

    @Bean
    public NewTopic deliveryCreatedTopic() {
        return TopicBuilder.name(deliveryCreatedTopic).partitions(partitions).replicas(replicationFactor).build();
    }

    @Bean
    public KafkaAdmin.NewTopics deliveryCreatedRetryTopics() {
        NewTopic[] topics = retryDelaysMs().stream()
                .map(delay -> TopicBuilder.name(deliveryCreatedTopic + "-retry-" + delay)
                        .partitions(partitions)
                        .replicas(replicationFactor)
                        .build())
                .toArray(NewTopic[]::new);
        return new KafkaAdmin.NewTopics(topics);
    }

    @Bean
    public NewTopic deliveryCreatedDltTopic() {
        return TopicBuilder.name(deliveryCreatedTopic + dltSuffix).partitions(partitions).replicas(replicationFactor).build();
    }

    private List<Long> retryDelaysMs() {
        List<Long> delays = new ArrayList<>();
        long delay = retryBackoffDelayMs;
        for (int attempt = 1; attempt < retryAttempts; attempt++) {
            delays.add(Math.min(delay, retryBackoffMaxDelayMs));
            delay = (long) (delay * retryBackoffMultiplier);
        }
        return delays;
    }
}
