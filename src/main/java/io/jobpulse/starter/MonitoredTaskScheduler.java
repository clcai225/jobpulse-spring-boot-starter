package io.jobpulse.starter;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

public final class MonitoredTaskScheduler implements TaskScheduler, ApplicationListener<ContextClosedEvent>, DisposableBean {

    private final TaskScheduler delegate;
    private final JobPulseReporter reporter;
    private final Set<String> registeredJobs = ConcurrentHashMap.newKeySet();

    public MonitoredTaskScheduler(TaskScheduler delegate, JobPulseReporter reporter) {
        this.delegate = delegate;
        this.reporter = reporter;
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable task, Trigger trigger) {
        return delegate.schedule(wrap(task, ScheduleDescriptor.of(trigger)), trigger);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable task, Instant startTime) {
        return delegate.schedule(wrap(task, "once:" + startTime), startTime);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Instant startTime, Duration period) {
        return delegate.scheduleAtFixedRate(wrap(task, "fixedRate:" + period), startTime, period);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Duration period) {
        return delegate.scheduleAtFixedRate(wrap(task, "fixedRate:" + period), period);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Instant startTime, Duration delay) {
        return delegate.scheduleWithFixedDelay(wrap(task, "fixedDelay:" + delay), startTime, delay);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Duration delay) {
        return delegate.scheduleWithFixedDelay(wrap(task, "fixedDelay:" + delay), delay);
    }

    private Runnable wrap(Runnable task, String schedule) {
        String jobKey = JobKeyResolver.resolve(task);
        if (registeredJobs.add(jobKey)) {
            reporter.register(new JobRegistration(jobKey, schedule));
        }
        return new ReportingRunnable(jobKey, schedule, task, reporter);
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        if (delegate instanceof ApplicationListener<?> listener) {
            @SuppressWarnings("unchecked")
            ApplicationListener<ContextClosedEvent> closedListener =
                    (ApplicationListener<ContextClosedEvent>) listener;
            closedListener.onApplicationEvent(event);
        }
    }

    @Override
    public void destroy() {
        if (delegate instanceof DisposableBean disposable) {
            try {
                disposable.destroy();
            } catch (Exception e) {
                throw new RuntimeException("Failed to destroy delegate TaskScheduler", e);
            }
        }
    }
}
