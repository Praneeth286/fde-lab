package com.fdelab.claimapi.api.dto;

import java.io.Serializable;

/** Cached in Redis — keep it small and serializable. */
public record MemberView(String memberId, String memberName, String plan, String state)
        implements Serializable {
}
