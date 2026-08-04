package ru.anton.springtests2.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "dead_letter_events", schema = "spring_test_s2")
public class DeadLetterEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_id")
    private UUID eventId;

    @Column(nullable = false, columnDefinition = "text")
    private String payload;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @Column(name = "received_at", nullable = false, updatable = false)
    private OffsetDateTime receivedAt = OffsetDateTime.now();
}
