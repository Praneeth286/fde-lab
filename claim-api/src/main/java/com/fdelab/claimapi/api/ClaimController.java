package com.fdelab.claimapi.api;

import com.fdelab.claimapi.api.dto.ClaimResponse;
import com.fdelab.claimapi.api.dto.ClaimSummary;
import com.fdelab.claimapi.api.dto.CreateClaimRequest;
import com.fdelab.claimapi.service.ClaimService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/claims")
public class ClaimController {

    private final ClaimService service;

    public ClaimController(ClaimService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ClaimResponse> submit(@Valid @RequestBody CreateClaimRequest request) {
        ClaimResponse body = ClaimResponse.from(service.submit(request));
        return ResponseEntity.created(URI.create("/claims/" + body.id()))
                .body(body);
    }

    @GetMapping("/{id}")
    public ClaimResponse byId(@PathVariable Long id) {
        return ClaimResponse.from(service.byId(id));
    }

    @GetMapping("/search")
    public List<ClaimSummary> search(
            @RequestParam String state,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "100") int limit) {
        return service.search(state, from, to, limit);
    }
}
