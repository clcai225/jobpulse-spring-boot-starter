package io.jobpulse.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jobpulse")
public class JobPulseProperties {

    private boolean enabled = true;
    private String apiUrl = "https://ingest.jobpulse.io";
    private String apiKey = "";
    private String appName = "";
    private String environment = "default";
    private boolean logOnly = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public boolean isLogOnly() {
        return logOnly;
    }

    public void setLogOnly(boolean logOnly) {
        this.logOnly = logOnly;
    }
}
