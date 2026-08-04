package ru.anton.springtests2.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import ru.anton.springtests2.dto.UserEnrichmentCreateDto;
import ru.anton.springtests2.model.DeadLetterEvent;
import ru.anton.springtests2.model.ProcessedEvent;
import ru.anton.springtests2.repository.DeadLetterEventRepository;
import ru.anton.springtests2.repository.ProcessedEventRepository;
import ru.anton.springtests2.repository.UserEnrichmentRepository;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCreatedEventListener {

    private final UserEnrichmentRepository userEnrichmentRepository;
    private final ObjectMapper objectMapper;
    private final ProcessedEventRepository processedEventRepository;
    private final TransactionTemplate transactionTemplate;
    private final DeadLetterEventRepository deadLetterEventRepository;

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
    public void onMessage(String payload,
                          @Header("eventId") String eventIdHeader) {
        UUID eventId = UUID.fromString(eventIdHeader);
        UserEnrichmentCreateDto dto = objectMapper.readValue(payload, UserEnrichmentCreateDto.class);

        transactionTemplate.executeWithoutResult(status -> processEvent(eventId, dto));
    }

    private void processEvent(UUID eventId, UserEnrichmentCreateDto dto) {
        if (processedEventRepository.existsByEventId(eventId)) {
            log.debug("Событие {} уже обработано ранее, пропускаем (дубль доставки)", eventId);
            return;
        }

        int inserted = userEnrichmentRepository.insertIfAbsent(
                UUID.randomUUID(), dto.getUserId(), dto.getDiscountCardNumber(), dto.getBalance());

        if (inserted == 0) {
            log.debug("UserEnrichment для userId {} уже существует, Kafka-событие {} пропущено",
                    dto.getUserId(), eventId);
        } else {
            log.debug("UserEnrichment для userId {} создан по Kafka-событию {}", dto.getUserId(), eventId);
        }

        processedEventRepository.save(new ProcessedEvent(eventId));
    }

    @DltHandler
    public void onDlt(String payload,
                      @Header(value = "eventId", required = false) String eventIdHeader,
                      Exception ex) {

        UUID eventId = null;
        try {
            if (eventIdHeader != null) {
                eventId = UUID.fromString(eventIdHeader);
            }
        } catch (IllegalArgumentException parseEx) {
            log.warn("Не удалось получить eventId из заголовка DLT-сообщения: {}", eventIdHeader);
        }

        DeadLetterEvent deadLetterEvent = new DeadLetterEvent();
        deadLetterEvent.setEventId(eventId);
        deadLetterEvent.setPayload(payload);
        deadLetterEvent.setErrorMessage(ex.getMessage());
        deadLetterEventRepository.save(deadLetterEvent);

        log.error("Событие {} сохранено в dead_letter_events после исчерпания попыток. Payload: {}, причина: {}",
                eventId, payload, ex.getMessage());
    }
}