package com.fdelab.claimapi.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import com.fdelab.claimapi.api.dto.MemberView;
import com.fdelab.claimapi.repo.MemberRepository;
@Service
public class MemberLookupService {
    private static final Logger log = LoggerFactory.getLogger(MemberLookupService.class);
    private final MemberRepository members;

    public MemberLookupService(MemberRepository members) {
        this.members = members;
    }

    @Cacheable(value = "members", key = "#memberId")
    public MemberView findMember(String memberId) {
        log.info("member_lookup_db member_id={}", memberId);
        return members.findById(memberId)
        .map(m -> new MemberView(m.getMemberId(), m.getMemberName(), m.getPlan(), m.getState()))
        .orElseThrow(() -> new NotFoundException("Member not found: " + memberId));
    }
}