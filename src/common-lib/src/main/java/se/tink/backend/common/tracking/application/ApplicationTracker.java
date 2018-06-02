package se.tink.backend.common.tracking.application;

import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationForm;

public interface ApplicationTracker {
    void track(Application application);
    void track(Application application, ApplicationForm form);
}
