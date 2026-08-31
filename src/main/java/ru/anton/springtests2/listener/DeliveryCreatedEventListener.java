package ru.anton.springtests2.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import ru.anton.springtests2.dto.DeliveryCreatedEventPayloadDto;
import ru.anton.springtests2.exception.NonRetryableException;
import ru.anton.springtests2.repository.DeadLetterEventRepository;
import ru.anton.springtests2.service.DeliveryEnrichmentService;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryCreatedEventListener {

    private final DeliveryEnrichmentService deliveryEnrichmentService;
    private final DeadLetterEventRepository deadLetterEventRepository;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;

    @RetryableTopic(
            attempts = "${kafka.topics.retry.attempts}",
            backOff = @BackOff(
                    delayString = "${kafka.topics.retry.backoff-delay-ms}",
                    multiplierString = "${kafka.topics.retry.backoff-multiplier}",
                    maxDelayString = "${kafka.topics.retry.backoff-max-delay-ms}"
            ),
            dltTopicSuffix = ".DLQ",
            autoCreateTopics = "false",
            exclude = NonRetryableException.class
    )
    @KafkaListener(
            topics = "${kafka.consumer.delivery-created-topic:delivery.created}",
            groupId = "${kafka.consumer.group-id:spring-test-s2-delivery-enrichment}"
    )
    public void onMessage(String payload,
                          @Header(value = "eventId", required = false) String eventIdHeader) {
        if (eventIdHeader == null || eventIdHeader.isBlank()) {
            throw new NonRetryableException("Отсутствует заголовок eventId у Kafka-сообщения");
        }

        UUID eventId;
        try {
            eventId = UUID.fromString(eventIdHeader);
        } catch (IllegalArgumentException ex) {
            throw new NonRetryableException("Невалидный eventId у Kafka-сообщения: " + eventIdHeader);
        }

        DeliveryCreatedEventPayloadDto dto;
        try {
            dto = objectMapper.readValue(payload, DeliveryCreatedEventPayloadDto.class);
        } catch (JacksonException ex) {
            throw new NonRetryableException("Невалидный payload Kafka-сообщения (eventId=" + eventId + "): " + ex.getMessage());
        }

        deliveryEnrichmentService.processDeliveryCreatedEvent(eventId, dto);
    }

    @DltHandler
    public void onDlt(String payload,
                      @Header(value = "eventId", required = false) String eventIdHeader,
                      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                      @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                      @Header(KafkaHeaders.OFFSET) long offset,
                      Exception ex) {

        UUID eventId = null;
        try {
            if (eventIdHeader != null) {
                eventId = UUID.fromString(eventIdHeader);
            }
        } catch (IllegalArgumentException parseEx) {
            log.warn("Не удалось распарсить eventId из заголовка DLT-сообщения: {}", eventIdHeader);
        }

        String messageKey = eventId != null ? eventId.toString() : topic + ":" + partition + ":" + offset;
        UUID finalEventId = eventId;

        try {
            transactionTemplate.executeWithoutResult(status ->
                    deadLetterEventRepository.upsertByMessageKey(UUID.randomUUID(), finalEventId, messageKey, payload, ex.getMessage()));
            log.error("Событие {} (ключ {}) сохранено в dead_letter_events. Payload: {}, причина: {}",
                    eventId, messageKey, payload, ex.getMessage());
        } catch (Exception persistEx) {
            log.error("КРИТИЧНО: не удалось сохранить DLT-событие (ключ {}) в БД, offset НЕ будет подтверждён. " +
                            "Payload: {}, исходная причина: {}, ошибка сохранения: {}",
                    messageKey, payload, ex.getMessage(), persistEx.getMessage(), persistEx);
            throw persistEx;
        }
    }
}
