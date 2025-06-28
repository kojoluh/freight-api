package com.fkluh.freight.v1.model.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostcodeByCountDto {
    @NotEmpty(message = "Recipient postcode must not be empty")
    private String recipientPostcode;
    @NotEmpty(message = "Delay count must not be empty")
    private long delayCount;

    public PostcodeByCountDto() {
    }

    public PostcodeByCountDto(String recipientPostcode, long delayCount) {
        this.recipientPostcode = recipientPostcode;
        this.delayCount = delayCount;
    }

}
