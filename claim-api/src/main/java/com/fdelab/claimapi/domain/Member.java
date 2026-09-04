package com.fdelab.claimapi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "members")
public class Member {

    @Id
    @Column(name = "member_id")
    private String memberId;

    @Column(name = "member_name", nullable = false)
    private String memberName;

    @Column(name = "plan", nullable = false)
    private String plan;

    @Column(name = "state", nullable = false)
    private String state;

    protected Member() {
    }

    public String getMemberId() { return memberId; }
    public String getMemberName() { return memberName; }
    public String getPlan() { return plan; }
    public String getState() { return state; }
}
