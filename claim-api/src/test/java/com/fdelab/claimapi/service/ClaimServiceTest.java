package com.fdelab.claimapi.service;

import com.fdelab.claimapi.api.dto.CreateClaimRequest;
import com.fdelab.claimapi.domain.Claim;
import com.fdelab.claimapi.repo.ClaimRepository;
import com.fdelab.claimapi.repo.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    ClaimRepository claims;

    @Mock
    MemberRepository members;

    @InjectMocks
    ClaimService service;

    private CreateClaimRequest request() {
        return new CreateClaimRequest("CLM0000000001", "M00000001", "1234567890",
                LocalDate.of(2025, 1, 15), new BigDecimal("120.50"));
    }

    @Test
    void rejects_duplicate_claim_number_without_saving() {
        when(claims.findByClaimNumber("CLM0000000001"))
                .thenReturn(Optional.of(new Claim("CLM0000000001", "M00000001", "1234567890",
                        LocalDate.of(2025, 1, 15), new BigDecimal("120.50"), "SUBMITTED")));

        assertThatThrownBy(() -> service.submit(request()))
                .isInstanceOf(DuplicateClaimException.class);

        verify(claims, never()).save(any());
    }

    @Test
    void missing_claim_id_raises_not_found() {
        when(claims.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.byId(42L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("42");
    }

    @Test
    void claim_starts_in_submitted_status() {
        Claim claim = new Claim("CLM0000000002", "M00000002", "1234567890",
                LocalDate.of(2025, 2, 1), new BigDecimal("10.00"), "SUBMITTED");

        assertThat(claim.getStatus()).isEqualTo("SUBMITTED");
        assertThat(claim.getCreatedAt()).isNotNull();
    }
}
