package se.tink.backend.export.model;

import java.util.List;
import se.tink.backend.export.model.submodels.ExportLocation;

public class UserLocations {

    private final List<ExportLocation> locations;

    public UserLocations(List<ExportLocation> locations) {
        this.locations = locations;
    }

    public List<ExportLocation> getLocations() {
        return locations;
    }
}
