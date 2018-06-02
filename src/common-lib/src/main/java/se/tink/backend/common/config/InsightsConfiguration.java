package se.tink.backend.common.config;

public class InsightsConfiguration {
    private boolean enabled = false;

    public InsightsConfiguration() {
    }

    public InsightsConfiguration(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
