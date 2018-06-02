package se.tink.backend.common.config;

public class ExportUserDataConfiguration {
    private boolean enabled = true;

    public ExportUserDataConfiguration() {
    }

    public ExportUserDataConfiguration(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
