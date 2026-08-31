package ru.anton.springtests2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.anton.springtests2.model.DeadLetterEvent;

import java.util.UUID;

public interface DeadLetterEventRepository extends JpaRepository<DeadLetterEvent, UUID> {
    @Modifying
    @Query(value = """
            INSERT INTO spring_test_s2.dead_letter_events (id, event_id, message_key, payload, error_message, received_at)
            VALUES (:id, :eventId, :messageKey, :payload, :errorMessage, now())
            ON CONFLICT (message_key) DO UPDATE SET
                payload = EXCLUDED.payload,
                error_message = EXCLUDED.error_message,
                received_at = now()
            """, nativeQuery = true)
    void upsertByMessageKey(@Param("id") UUID id,
                            @Param("eventId") UUID eventId,
                            @Param("messageKey") String messageKey,
                            @Param("payload") String payload,
                            @Param("errorMessage") String errorMessage);
}