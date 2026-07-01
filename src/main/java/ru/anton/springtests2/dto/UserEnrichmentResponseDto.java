package ru.anton.springtests2.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class UserEnrichmentResponseDto {

    private UUID id;
    private UUID userId;
    private String discountCardNumber;
    private BigDecimal balance;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
