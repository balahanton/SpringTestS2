package ru.anton.springtests2.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicsConfig {

    @Bean
    public NewTopic userCreatedTopic() {
        return new NewTopic("user.created", 1, (short) 1);
    }

    @Bean
    public NewTopic userCreatedRetry1000Topic() {
        return new NewTopic("user.created-retry-1000", 1, (short) 1);
    }

    @Bean
    public NewTopic userCreatedRetry2000Topic() {
        return new NewTopic("user.created-retry-2000", 1, (short) 1);
    }

    @Bean
    public NewTopic userCreatedRetry4000Topic() {
        return new NewTopic("user.created-retry-4000", 1, (short) 1);
    }

    @Bean
    public NewTopic userCreatedDltTopic() {
        return new NewTopic("user.created.DLQ", 1, (short) 1);
    }
}
