package io.jobpulse.starter;

final class ReportingRunnable implements Runnable {

    private final String jobKey;
    private final String schedule;
    private final Runnable delegate;
    private final JobPulseReporter reporter;

    ReportingRunnable(String jobKey, String schedule, Runnable delegate, JobPulseReporter reporter) {
        this.jobKey = jobKey;
        this.schedule = schedule;
        this.delegate = delegate;
        this.reporter = reporter;
    }

    @Override
    public void run() {
        long startedAt = System.currentTimeMillis();
        JobStatus status = JobStatus.SUCCESS;
        String errorClass = null;
        String errorMessage = null;
        try {
            delegate.run();
        } catch (Throwable t) {
            status = JobStatus.FAILURE;
            errorClass = t.getClass().getName();
            errorMessage = String.valueOf(t.getMessage());
            throw t;
        } finally {
            try {
                reporter.report(new JobExecution(jobKey, schedule, status,
                        startedAt, System.currentTimeMillis() - startedAt, errorClass, errorMessage));
            } catch (RuntimeException swallowed) {
                // reporting must never affect the job
            }
        }
    }
}
