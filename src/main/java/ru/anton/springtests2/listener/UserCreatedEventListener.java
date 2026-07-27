package ru.anton.springtests2.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.anton.springtests2.dto.UserEnrichmentCreateDto;
import ru.anton.springtests2.repository.UserEnrichmentRepository;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCreatedEventListener {

    private final UserEnrichmentRepository userEnrichmentRepository;
    private final ObjectMapper objectMapper;

    @RetryableTopic(
            attempts = "4",
            backOff = @BackOff(delay = 1000, multiplier = 2.0, maxDelay = 30000),
            dltTopicSuffix = ".DLQ",
            autoCreateTopics = "false"
    )
    @KafkaListener(
            topics = "${kafka.consumer.user-created-topic:user.created}",
            groupId = "${kafka.consumer.group-id:spring-test-s2-enrichment}"
    )
    @Transactional
    public void onMessage(String payload, Acknowledgment ack) {
        UserEnrichmentCreateDto dto = objectMapper.readValue(payload, UserEnrichmentCreateDto.class);

        int inserted = userEnrichmentRepository.insertIfAbsent(
                UUID.randomUUID(), dto.getUserId(), dto.getDiscountCardNumber(), dto.getBalance());

        if (inserted == 0) {
            log.info("UserEnrichment для userId {} уже существует (создан по HTTP), Kafka-событие пропущено",
                    dto.getUserId());
        } else {
            log.info("UserEnrichment для userId {} создан по Kafka-событию", dto.getUserId());
        }

        ack.acknowledge();
    }

    @DltHandler
    public void onDlt(String payload, Exception ex) {
        log.error("Событие ушло в DLQ после исчерпания попыток. Payload: {}, причина: {}",
                payload, ex.getMessage());
    }

}
