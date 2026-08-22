package ru.anton.springtests2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.anton.springtests2.model.DeliveryEnrichment;

import java.util.UUID;

public interface DeliveryEnrichmentRepository extends JpaRepository<DeliveryEnrichment, UUID> {

    @Modifying
    @Query(value = """
            INSERT INTO spring_test_s2.delivery_enrichments (id, delivery_id, address, status, received_at)
            VALUES (:id, :deliveryId, :address, :status, now())
            ON CONFLICT (delivery_id) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(@Param("id") UUID id,
                       @Param("deliveryId") UUID deliveryId,
                       @Param("address") String address,
                       @Param("status") String status);
}
