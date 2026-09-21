package io.jobpulse.starter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class RecordingReporter implements JobPulseReporter {

    final List<JobRegistration> registrations = new CopyOnWriteArrayList<>();
    final List<JobExecution> executions = new CopyOnWriteArrayList<>();

    @Override
    public void register(JobRegistration registration) {
        registrations.add(registration);
    }

    @Override
    public void report(JobExecution execution) {
        executions.add(execution);
    }
}
