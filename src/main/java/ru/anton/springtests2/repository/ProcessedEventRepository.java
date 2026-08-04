package ru.anton.springtests2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.anton.springtests2.model.ProcessedEvent;

import java.util.UUID;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {
    boolean existsByEventId(UUID eventId);
}
