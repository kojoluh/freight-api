package com.fkluh.freight.v1.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class PackageDto {
    @NotNull
    @NotEmpty(message = "Tracking number must not be empty")
    private String trackingNumber;

    @Email(message = "Email should be valid")
    @NotEmpty(message = "Email cannot not be null or empty")
    private String email;

    @NotEmpty(message = "Recipient postcode cannot be null or empty")
    private String recipientPostcode;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Estimated delivery date cannot be null or empty")
    private LocalDate estimatedDeliveryDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualDeliveryDate;

    private String status;
}
