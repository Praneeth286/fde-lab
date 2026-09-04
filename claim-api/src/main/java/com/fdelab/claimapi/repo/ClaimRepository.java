package com.fdelab.claimapi.repo;

import com.fdelab.claimapi.api.dto.ClaimSummary;
import com.fdelab.claimapi.domain.Claim;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ClaimRepository extends JpaRepository<Claim, Long> {

    Optional<Claim> findByClaimNumber(String claimNumber);

    /**
     * The "slow endpoint" for Milestone 1.
     * There is no index on claims.member_id or members.state — run EXPLAIN ANALYZE
     * on the generated SQL, add the right index, and record both timings.
     */
    @Query("""
            SELECT new com.fdelab.claimapi.api.dto.ClaimSummary(
                       c.claimNumber, c.serviceDate, c.amount, c.status, m.memberName, m.state)
            FROM Claim c
            JOIN Member m ON m.memberId = c.memberId
            WHERE m.state = :state
              AND c.serviceDate BETWEEN :from AND :to
            ORDER BY c.serviceDate DESC
            """)
    List<ClaimSummary> search(@Param("state") String state,
                              @Param("from") LocalDate from,
                              @Param("to") LocalDate to,
                              Pageable pageable);
}
