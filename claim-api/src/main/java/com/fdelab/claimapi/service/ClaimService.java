package com.fdelab.claimapi.service;

import com.fdelab.claimapi.api.dto.ClaimSummary;
import com.fdelab.claimapi.api.dto.CreateClaimRequest;
import com.fdelab.claimapi.api.dto.MemberView;
import com.fdelab.claimapi.domain.Claim;
import com.fdelab.claimapi.repo.ClaimRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ClaimService {

    private static final Logger log = LoggerFactory.getLogger(ClaimService.class);

    private final ClaimRepository claims;
    private final MemberLookupService memberLookup;

    public ClaimService(ClaimRepository claims, MemberLookupService memberLookup) {
    this.claims = claims;
    this.memberLookup = memberLookup;
}

    @Transactional
    public Claim submit(CreateClaimRequest req) {
        claims.findByClaimNumber(req.claimNumber()).ifPresent(existing -> {
            throw new DuplicateClaimException("claim " + req.claimNumber() + " already exists");
        });

        MemberView member = memberLookup.findMember(req.memberId());

        Claim claim = new Claim(req.claimNumber(), member.memberId(), req.providerNpi(),
                req.serviceDate(), req.amount(), "SUBMITTED");
        Claim saved = claims.save(claim);
        log.info("claim_submitted claim_number={} member_id={} amount={}",
                saved.getClaimNumber(), saved.getMemberId(), saved.getAmount());
        return saved;
    }

    @Transactional(readOnly = true)
    public Claim byId(Long id) {
        return claims.findById(id)
                .orElseThrow(() -> new NotFoundException("claim " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public List<ClaimSummary> search(String state, LocalDate from, LocalDate to, int limit) {
        long started = System.nanoTime();
        List<ClaimSummary> results = claims.search(state, from, to, PageRequest.of(0, limit));
        long millis = (System.nanoTime() - started) / 1_000_000;
        log.info("claim_search state={} from={} to={} results={} took_ms={}",
                state, from, to, results.size(), millis);
        return results;
    }

}
