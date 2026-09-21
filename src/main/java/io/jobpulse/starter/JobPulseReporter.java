package io.jobpulse.starter;

public interface JobPulseReporter {

    void register(JobRegistration registration);

    void report(JobExecution execution);
}
