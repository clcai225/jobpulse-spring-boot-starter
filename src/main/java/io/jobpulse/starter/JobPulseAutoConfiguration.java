package io.jobpulse.starter;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@AutoConfiguration
@ConditionalOnProperty(prefix = "jobpulse", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(JobPulseProperties.class)
public class JobPulseAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(JobPulseReporter.class)
    public HttpJobPulseReporter jobPulseReporter(JobPulseProperties properties, Environment environment) {
        if (properties.getAppName() == null || properties.getAppName().isBlank()) {
            properties.setAppName(environment.getProperty("spring.application.name", "unknown"));
        }
        return new HttpJobPulseReporter(properties);
    }

    @Bean
    public static JobPulseBeanPostProcessor jobPulseBeanPostProcessor(JobPulseReporter reporter) {
        return new JobPulseBeanPostProcessor(reporter);
    }
}
