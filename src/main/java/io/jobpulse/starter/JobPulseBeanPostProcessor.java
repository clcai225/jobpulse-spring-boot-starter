package io.jobpulse.starter;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.scheduling.TaskScheduler;

public final class JobPulseBeanPostProcessor implements BeanPostProcessor {

    private final JobPulseReporter reporter;

    public JobPulseBeanPostProcessor(JobPulseReporter reporter) {
        this.reporter = reporter;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof TaskScheduler scheduler && !(bean instanceof MonitoredTaskScheduler)) {
            return new MonitoredTaskScheduler(scheduler, reporter);
        }
        return bean;
    }
}
