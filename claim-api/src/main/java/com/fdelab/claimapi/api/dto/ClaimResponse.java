package com.fdelab.claimapi.api.dto;

import com.fdelab.claimapi.domain.Claim;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ClaimResponse(
        Long id,
        String claimNumber,
        String memberId,
        String providerNpi,
        LocalDate serviceDate,
        BigDecimal amount,
        String status,
        Instant createdAt) {

    public static ClaimResponse from(Claim c) {
        return new ClaimResponse(c.getId(), c.getClaimNumber(), c.getMemberId(),
                c.getProviderNpi(), c.getServiceDate(), c.getAmount(),
                c.getStatus(), c.getCreatedAt());
    }
}
