package ru.anton.springtests2.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.List;

@Configuration
public class KafkaTopicsConfig {

    @Value("${kafka.consumer.delivery-created-topic}")
    private String deliveryCreatedTopic;

    @Value("${kafka.topics.partitions:1}")
    private int partitions;

    @Value("${kafka.topics.replication-factor:1}")
    private short replicationFactor;

    @Value("${kafka.topics.retry-delays-ms}")
    private List<Integer> retryDelaysMs;

    @Value("${kafka.topics.dlt-suffix:.DLQ}")
    private String dltSuffix;

    @Bean
    public NewTopic deliveryCreatedTopic() {
        return TopicBuilder.name(deliveryCreatedTopic).partitions(partitions).replicas(replicationFactor).build();
    }

    @Bean
    public KafkaAdmin.NewTopics deliveryCreatedRetryTopics() {
        NewTopic[] topics = retryDelaysMs.stream()
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
}
