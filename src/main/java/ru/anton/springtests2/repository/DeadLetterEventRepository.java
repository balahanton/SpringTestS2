package ru.anton.springtests2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.anton.springtests2.model.DeadLetterEvent;

import java.util.UUID;

public interface DeadLetterEventRepository extends JpaRepository<DeadLetterEvent, UUID> {
}
