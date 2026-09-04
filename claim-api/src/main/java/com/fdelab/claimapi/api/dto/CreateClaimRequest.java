package com.fdelab.claimapi.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateClaimRequest(

        @NotBlank
        @Pattern(regexp = "CLM[0-9]{10}", message = "must match CLM + 10 digits")
        String claimNumber,

        @NotBlank
        String memberId,

        @NotBlank
        @Pattern(regexp = "[0-9]{10}", message = "NPI must be 10 digits")
        String providerNpi,

        @NotNull
        @PastOrPresent
        LocalDate serviceDate,

        @NotNull
        @DecimalMin(value = "0.01", message = "amount must be positive")
        BigDecimal amount) {
}
