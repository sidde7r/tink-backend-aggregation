package se.tink.libraries.draining;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ApplicationDrainMode {
    // Make modifications visible to all threads immediately after update
    private volatile boolean enabled = false;

    @Inject
    public ApplicationDrainMode() {}

    public boolean isEnabled() {
        return this.enabled;
    }

    protected void enable() {
        this.enabled = true;
    }

    protected void disable() {
        this.enabled = false;
    }

    @Override
    public String toString() {
        if (this.isEnabled()) {
            return "enabled";
        } else {
            return "disabled";
        }
    }
}
