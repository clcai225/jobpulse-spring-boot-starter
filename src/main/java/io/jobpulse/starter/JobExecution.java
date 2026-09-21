package io.jobpulse.starter;

public record JobExecution(
        String jobKey,
        String schedule,
        JobStatus status,
        long startedAt,
        long durationMs,
        String errorClass,
        String errorMessage) {
}
