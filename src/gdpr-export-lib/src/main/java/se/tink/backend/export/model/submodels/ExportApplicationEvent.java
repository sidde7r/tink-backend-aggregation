package se.tink.backend.export.model.submodels;

import java.util.Date;
import se.tink.backend.export.helper.DefaultSetter;

public class ExportApplicationEvent implements DefaultSetter {

    private final String updated;
    private final String applicationType;
    private final String applicationStatus;

    public ExportApplicationEvent(Date updated, String applicationType, String applicationStatus) {
        this.updated = notNull(updated);
        this.applicationType = applicationType;
        this.applicationStatus = applicationStatus;
    }

    public String getUpdated() {
        return updated;
    }

    public String getApplicationType() {
        return applicationType;
    }

    public String getApplicationStatus() {
        return applicationStatus;
    }
}
