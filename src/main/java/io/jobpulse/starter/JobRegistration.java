package io.jobpulse.starter;

public record JobRegistration(String jobKey, String schedule, long registeredAt) {

    public JobRegistration(String jobKey, String schedule) {
        this(jobKey, schedule, System.currentTimeMillis());
    }
}
