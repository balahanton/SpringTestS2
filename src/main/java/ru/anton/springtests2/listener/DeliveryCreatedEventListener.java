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
import ru.anton.springtests2.repository.DeadLetterEventRepository;
import ru.anton.springtests2.repository.DeliveryEnrichmentRepository;
import ru.anton.springtests2.repository.ProcessedEventRepository;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryCreatedEventListener {

    private final DeliveryEnrichmentRepository deliveryEnrichmentRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final DeadLetterEventRepository deadLetterEventRepository;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;

    @RetryableTopic(
            attempts = "4",
            backOff = @BackOff(delay = 1000, multiplier = 2.0, maxDelay = 30000),
            dltTopicSuffix = ".DLQ",
            autoCreateTopics = "false"
    )
    @KafkaListener(
            topics = "${kafka.consumer.delivery-created-topic:delivery.created}",
            groupId = "${kafka.consumer.group-id:spring-test-s2-delivery-enrichment}"
    )
    public void onMessage(String payload,
                          @Header("eventId") String eventIdHeader) {
        UUID eventId = UUID.fromString(eventIdHeader);
        DeliveryCreatedEventPayloadDto dto = objectMapper.readValue(payload, DeliveryCreatedEventPayloadDto.class);

        transactionTemplate.executeWithoutResult(status -> processEvent(eventId, dto));
    }

    private void processEvent(UUID eventId, DeliveryCreatedEventPayloadDto dto) {
        int insertedProcessedEvent = processedEventRepository.insertIfAbsent(eventId);

        if (insertedProcessedEvent == 0) {
            log.debug("Событие {} уже обработано ранее (или обрабатывается параллельно), пропускаем", eventId);
            return;
        }

        int inserted = deliveryEnrichmentRepository.insertIfAbsent(
                UUID.randomUUID(), dto.getDeliveryId(), dto.getAddress(), dto.getStatus());

        if (inserted == 0) {
            log.debug("DeliveryEnrichment для deliveryId {} уже существует, событие {} пропущено",
                    dto.getDeliveryId(), eventId);
        } else {
            log.debug("DeliveryEnrichment для deliveryId {} создан по событию {}", dto.getDeliveryId(), eventId);
        }
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

        try {
            deadLetterEventRepository.upsertByMessageKey(UUID.randomUUID(), eventId, messageKey, payload, ex.getMessage());
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
