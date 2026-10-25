package com.allypay.fineract_integration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateClientRequest(
        @NotNull @Positive Long officeId,
        @NotBlank String firstname,
        @NotBlank String lastname,
        String externalId
) {
}
