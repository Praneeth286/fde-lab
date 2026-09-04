package com.fdelab.claimapi.repo;

import com.fdelab.claimapi.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, String> {
}
