package se.tink.backend.export.model;

import java.util.List;
import se.tink.backend.export.model.submodels.ExportProperty;

public class Properties {

    private final List<ExportProperty> properties;

    public Properties(List<ExportProperty> properties) {
        this.properties = properties;
    }

    public List<ExportProperty> getProperties() {
        return properties;
    }
}
