package ru.anton.springtests2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.anton.springtests2.dto.DeliveryCreatedEventPayloadDto;
import ru.anton.springtests2.repository.DeliveryEnrichmentRepository;
import ru.anton.springtests2.repository.ProcessedEventRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryEnrichmentService {

    private final DeliveryEnrichmentRepository deliveryEnrichmentRepository;
    private final ProcessedEventRepository processedEventRepository;

    @Transactional
    public void processDeliveryCreatedEvent(UUID eventId, DeliveryCreatedEventPayloadDto dto) {
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
}
