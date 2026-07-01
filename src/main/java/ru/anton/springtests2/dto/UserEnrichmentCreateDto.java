package ru.anton.springtests2.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class UserEnrichmentCreateDto {

    @NotNull(message = "ID юзера обязателен")
    private UUID userId;

    @NotBlank(message = "Номер дисконт карты обязателен")
    private String discountCardNumber;

    @NotNull(message = "Баланс обязателен")
    @DecimalMin(value = "0.0", message = "Баланс не может быть отрицательным")
    private BigDecimal balance;
}
