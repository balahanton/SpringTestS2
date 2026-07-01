package ru.anton.springtests2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.anton.springtests2.model.UserEnrichment;

import java.util.Optional;
import java.util.UUID;

public interface UserEnrichmentRepository extends JpaRepository<UserEnrichment, UUID> {

    Optional<UserEnrichment> findByUserId(UUID userId);
}
