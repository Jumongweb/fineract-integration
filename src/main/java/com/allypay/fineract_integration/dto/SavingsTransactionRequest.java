
package com.allypay.fineract_integration.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record SavingsTransactionRequest(
        @NotNull
        @DecimalMin("0.01")
        @Digits(integer = 15, fraction = 2)
        BigDecimal amount,

        @NotNull
        @Positive
        Long paymentTypeId
) {}
