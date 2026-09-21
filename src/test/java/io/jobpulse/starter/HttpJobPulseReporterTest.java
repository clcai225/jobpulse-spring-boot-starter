package io.jobpulse.starter;

import org.junit.jupiter.api.Test;

class HttpJobPulseReporterTest {

    @Test
    void logOnlyModeNeverThrows() {
        JobPulseProperties props = new JobPulseProperties();
        props.setLogOnly(true);
        HttpJobPulseReporter reporter = new HttpJobPulseReporter(props);

        reporter.register(new JobRegistration("com.acme.Reports#nightly", "cron:0 0 2 * * *"));
        reporter.report(new JobExecution("com.acme.Reports#nightly", "cron:0 0 2 * * *",
                JobStatus.FAILURE, System.currentTimeMillis(), 120,
                "java.lang.IllegalStateException", "connection refused"));

        reporter.shutdown();
    }

    @Test
    void missingApiKeyDisablesReportingWithoutThrowing() {
        JobPulseProperties props = new JobPulseProperties();
        HttpJobPulseReporter reporter = new HttpJobPulseReporter(props);

        reporter.report(new JobExecution("job", "fixedRate:PT5M", JobStatus.SUCCESS,
                System.currentTimeMillis(), 10, null, null));

        reporter.shutdown();
    }
}
