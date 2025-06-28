package com.fkluh.freight.v1.model.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
public class PackageUpdateDto {
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotEmpty(message = "Actual delivery date must not be empty")
    String actualDeliveryDate;
}
