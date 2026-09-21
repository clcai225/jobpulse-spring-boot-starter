package io.jobpulse.starter;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HttpJobPulseReporter implements JobPulseReporter {

    private static final Logger log = LoggerFactory.getLogger(HttpJobPulseReporter.class);
    private static final int MAX_ERROR_MESSAGE_LENGTH = 512;

    private final JobPulseProperties props;
    private final HttpClient httpClient;
    private final ThreadPoolExecutor executor;
    private volatile boolean keyWarningLogged;

    public HttpJobPulseReporter(JobPulseProperties props) {
        this.props = props;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        this.executor = new ThreadPoolExecutor(
                1, 1, 60, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(256),
                r -> {
                    Thread thread = new Thread(r, "jobpulse-reporter");
                    thread.setDaemon(true);
                    return thread;
                },
                new ThreadPoolExecutor.DiscardOldestPolicy());
    }

    @Override
    public void register(JobRegistration registration) {
        String body = "{\"app\":" + quote(appName())
                + ",\"env\":" + quote(props.getEnvironment())
                + ",\"jobKey\":" + quote(registration.jobKey())
                + ",\"schedule\":" + quote(registration.schedule())
                + ",\"registeredAt\":" + registration.registeredAt() + "}";
        send("/v1/jobs/register", body);
    }

    @Override
    public void report(JobExecution execution) {
        String errorClass = execution.errorClass() == null ? "null" : quote(execution.errorClass());
        String errorMessage = execution.errorMessage() == null ? "null" : quote(truncate(execution.errorMessage()));
        String body = "{\"app\":" + quote(appName())
                + ",\"env\":" + quote(props.getEnvironment())
                + ",\"jobKey\":" + quote(execution.jobKey())
                + ",\"schedule\":" + quote(execution.schedule())
                + ",\"status\":" + quote(execution.status().name())
                + ",\"startedAt\":" + execution.startedAt()
                + ",\"durationMs\":" + execution.durationMs()
                + ",\"errorClass\":" + errorClass
                + ",\"errorMessage\":" + errorMessage + "}";
        send("/v1/executions", body);
    }

    private void send(String path, String json) {
        if (props.isLogOnly()) {
            log.info("[jobpulse] POST {} {}", path, json);
            return;
        }
        if (props.getApiKey() == null || props.getApiKey().isBlank()) {
            if (!keyWarningLogged) {
                keyWarningLogged = true;
                log.warn("[jobpulse] jobpulse.api-key is not set; reporting is disabled. "
                        + "Set the API key, or use jobpulse.log-only=true for local debugging.");
            }
            return;
        }
        executor.execute(() -> deliver(path, json));
    }

    private void deliver(String path, String json) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(props.getApiUrl() + path))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + props.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                    .whenComplete((response, error) -> {
                        if (error != null) {
                            log.debug("[jobpulse] failed to report: {}", String.valueOf(error.getCause()));
                        } else if (response.statusCode() >= 400) {
                            log.debug("[jobpulse] ingest returned HTTP {}", response.statusCode());
                        }
                    });
        } catch (Exception e) {
            log.debug("[jobpulse] failed to queue report", e);
        }
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
        try {
            executor.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String appName() {
        return props.getAppName() == null || props.getAppName().isBlank() ? "unknown" : props.getAppName();
    }

    private static String quote(String value) {
        return "\"" + Json.escape(value) + "\"";
    }

    private static String truncate(String value) {
        return value.length() <= MAX_ERROR_MESSAGE_LENGTH ? value : value.substring(0, MAX_ERROR_MESSAGE_LENGTH);
    }
}
