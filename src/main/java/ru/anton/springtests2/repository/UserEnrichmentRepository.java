package ru.anton.springtests2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.anton.springtests2.model.UserEnrichment;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface UserEnrichmentRepository extends JpaRepository<UserEnrichment, UUID> {

    Optional<UserEnrichment> findByUserId(UUID userId);

    @Modifying
    @Query(value = """
        INSERT INTO spring_test_s2.user_enrichments
            (id, user_id, discount_card_number, balance, created_at, is_deleted)
        VALUES
            (:id, :userId, :discountCardNumber, :balance, now(), false)
        ON CONFLICT (user_id) WHERE (is_deleted = false) DO NOTHING
        """, nativeQuery = true)
    int insertIfAbsent(@Param("id") UUID id,
                       @Param("userId") UUID userId,
                       @Param("discountCardNumber") String discountCardNumber,
                       @Param("balance") BigDecimal balance);
}
