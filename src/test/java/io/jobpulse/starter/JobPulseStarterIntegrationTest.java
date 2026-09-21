package io.jobpulse.starter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MapPropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class JobPulseStarterIntegrationTest {

    private final RecordingReporter reporter = new RecordingReporter();
    private AnnotationConfigApplicationContext context;

    @AfterEach
    void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    void discoversJobsAndReportsSuccessAndFailure() throws Exception {
        startContext(Map.of("jobpulse.app-name", "test-app"));

        Thread.sleep(800);

        assertThat(reporter.registrations)
                .extracting(JobRegistration::jobKey)
                .contains(Jobs.class.getName() + "#every50ms", Jobs.class.getName() + "#alwaysFails");

        List<JobExecution> successRuns = reporter.executions.stream()
                .filter(e -> e.jobKey().endsWith("#every50ms") && e.status() == JobStatus.SUCCESS)
                .toList();
        assertThat(successRuns).isNotEmpty();

        List<JobExecution> failures = reporter.executions.stream()
                .filter(e -> e.jobKey().endsWith("#alwaysFails") && e.status() == JobStatus.FAILURE)
                .toList();
        assertThat(failures).isNotEmpty();
        assertThat(failures.get(0).errorClass()).isEqualTo("java.lang.IllegalStateException");
        assertThat(failures.get(0).errorMessage()).isEqualTo("boom");

        assertThat(TestConfig.instance.successRan).isTrue();
    }

    @Test
    void cronScheduleMetadataIsCaptured() throws Exception {
        startContext(Map.of("jobpulse.app-name", "test-app"));

        Thread.sleep(1300);

        JobRegistration cronRegistration = reporter.registrations.stream()
                .filter(r -> r.jobKey().endsWith("#everySecond"))
                .findFirst()
                .orElseThrow();
        assertThat(cronRegistration.schedule()).isEqualTo("cron:* * * * * *");

        assertThat(reporter.executions.stream()
                .filter(e -> e.jobKey().endsWith("#everySecond") && e.status() == JobStatus.SUCCESS)
                .findFirst()).isPresent();
    }

    private void startContext(Map<String, Object> properties) {
        TestConfig.reporter = reporter;
        context = new AnnotationConfigApplicationContext();
        context.getEnvironment().getPropertySources()
                .addFirst(new MapPropertySource("test", properties));
        context.register(TestConfig.class, JobPulseAutoConfiguration.class);
        context.refresh();
    }

    @Configuration
    @EnableScheduling
    static class TestConfig {

        static RecordingReporter reporter;
        static Jobs instance;

        @Bean
        ThreadPoolTaskScheduler taskScheduler() {
            ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
            scheduler.setPoolSize(2);
            return scheduler;
        }

        @Bean
        Jobs jobs() {
            instance = new Jobs();
            return instance;
        }

        @Bean
        RecordingReporter jobPulseReporter() {
            return reporter;
        }
    }

    static class Jobs {

        final AtomicBoolean successRan = new AtomicBoolean();

        @Scheduled(fixedRate = 50)
        void every50ms() {
            successRan.set(true);
        }

        @Scheduled(fixedRate = 80, initialDelay = 50)
        void alwaysFails() {
            throw new IllegalStateException("boom");
        }

        @Scheduled(cron = "* * * * * *")
        void everySecond() {
        }
    }
}
