package com.fdelab.claimapi.health;

import com.fdelab.claimapi.repo.ClaimRepository;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * A custom readiness signal: the service is only useful if it can reach data.
 * Stop Postgres and watch /actuator/health flip to DOWN.
 */
@Component("seedData")
public class SeedDataHealthIndicator implements HealthIndicator {

    private final ClaimRepository claims;

    public SeedDataHealthIndicator(ClaimRepository claims) {
        this.claims = claims;
    }

    @Override
    public Health health() {
        try {
            long count = claims.count();
            if (count == 0) {
                return Health.down().withDetail("claims", 0)
                        .withDetail("reason", "no seed data").build();
            }
            return Health.up().withDetail("claims", count).build();
        } catch (Exception ex) {
            return Health.down(ex).build();
        }
    }
}
