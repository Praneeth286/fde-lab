package com.fdelab.claimapi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "claims")
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "claim_number", nullable = false, unique = true)
    private String claimNumber;

    @Column(name = "member_id", nullable = false)
    private String memberId;

    @Column(name = "provider_npi", nullable = false)
    private String providerNpi;

    @Column(name = "service_date", nullable = false)
    private LocalDate serviceDate;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected Claim() {
    }

    public Claim(String claimNumber, String memberId, String providerNpi,
                 LocalDate serviceDate, BigDecimal amount, String status) {
        this.claimNumber = claimNumber;
        this.memberId = memberId;
        this.providerNpi = providerNpi;
        this.serviceDate = serviceDate;
        this.amount = amount;
        this.status = status;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getClaimNumber() { return claimNumber; }
    public String getMemberId() { return memberId; }
    public String getProviderNpi() { return providerNpi; }
    public LocalDate getServiceDate() { return serviceDate; }
    public BigDecimal getAmount() { return amount; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }

    public void setStatus(String status) { this.status = status; }
}
