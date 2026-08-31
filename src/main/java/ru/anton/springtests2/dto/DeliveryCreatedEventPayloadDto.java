package ru.anton.springtests2.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeliveryCreatedEventPayloadDto {

    private UUID deliveryId;
    private String address;
    private String status;
}
