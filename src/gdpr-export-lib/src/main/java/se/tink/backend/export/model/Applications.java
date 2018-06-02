package se.tink.backend.export.model;

import java.util.List;
import se.tink.backend.export.model.submodels.ExportApplication;

public class Applications {

    private final List<ExportApplication> applications;

    public Applications(
            List<ExportApplication> applications) {
        this.applications = applications;
    }

    public List<ExportApplication> getApplications() {
        return applications;
    }
}
