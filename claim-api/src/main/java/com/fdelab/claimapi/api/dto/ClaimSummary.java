package com.fdelab.claimapi.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ClaimSummary(
        String claimNumber,
        LocalDate serviceDate,
        BigDecimal amount,
        String status,
        String memberName,
        String state) {
}
