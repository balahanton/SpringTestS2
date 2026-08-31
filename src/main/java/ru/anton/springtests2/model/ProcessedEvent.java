package ru.anton.springtests2.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "processed_events", schema = "spring_test_s2")
public class ProcessedEvent {

    @Id
    private UUID eventId;

    @Column(nullable = false)
    private OffsetDateTime processedAt = OffsetDateTime.now();

    public ProcessedEvent(UUID eventId) {
        this.eventId = eventId;
    }
}
