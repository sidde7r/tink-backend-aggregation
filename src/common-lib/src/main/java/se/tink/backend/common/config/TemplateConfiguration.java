package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class TemplateConfiguration {
    private boolean warmUp = false;
    private boolean enabled = true;
    private int numEnginesWarmUp = 6;
    private int numEnginesMaxActive = 30;

    public boolean isWarmUp() {
        return warmUp;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getNumEnginesWarmUp() {
        return numEnginesWarmUp;
    }

    public int getNumEnginesMaxActive() {
        return numEnginesMaxActive;
    }
}
