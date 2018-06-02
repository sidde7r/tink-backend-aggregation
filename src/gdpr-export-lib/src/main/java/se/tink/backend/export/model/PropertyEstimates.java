package se.tink.backend.export.model;

import java.util.List;
import se.tink.backend.export.model.submodels.ExportPropertyEstimate;

public class PropertyEstimates {
    private final List<ExportPropertyEstimate> estimates;

    public PropertyEstimates(List<ExportPropertyEstimate> estimates) {
        this.estimates = estimates;
    }

    public List<ExportPropertyEstimate> getEstimates() {
        return estimates;
    }
}
