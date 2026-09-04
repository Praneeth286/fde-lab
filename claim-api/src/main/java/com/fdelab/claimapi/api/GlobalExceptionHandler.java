package com.fdelab.claimapi.api;

import com.fdelab.claimapi.service.DuplicateClaimException;
import com.fdelab.claimapi.service.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One place where every error response shape is decided.
 * Talk about this in interviews: consistent contract, no stack traces leaked to callers,
 * correlation-friendly logging on the server side.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex, HttpServletRequest req) {
        return problem(HttpStatus.NOT_FOUND, "Resource not found", ex.getMessage(), req);
    }

    @ExceptionHandler(DuplicateClaimException.class)
    public ProblemDetail handleDuplicate(DuplicateClaimException ex, HttpServletRequest req) {
        return problem(HttpStatus.CONFLICT, "Duplicate claim", ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> fields.put(fe.getField(), fe.getDefaultMessage()));
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "Validation failed",
                "One or more fields are invalid", req);
        pd.setProperty("fields", fields);
        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex, HttpServletRequest req) {
        log.error("unhandled_exception path={} type={}", req.getRequestURI(),
                ex.getClass().getSimpleName(), ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error",
                "Unexpected error. Check server logs.", req);
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        pd.setType(URI.create("https://fde-lab.local/errors/" + status.value()));
        pd.setInstance(URI.create(req.getRequestURI()));
        pd.setProperty("timestamp", Instant.now().toString());
        return pd;
    }
}
