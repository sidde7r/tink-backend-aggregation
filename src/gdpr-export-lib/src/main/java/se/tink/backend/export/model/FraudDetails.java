package se.tink.backend.export.model;

import java.util.List;
import se.tink.backend.export.model.submodels.ExportFraud;

public class FraudDetails {

    private final List<ExportFraud> fraudDetails;

    public FraudDetails(List<ExportFraud> fraudDetails) {
        this.fraudDetails = fraudDetails;
    }

    public List<ExportFraud> getFraudDetails() {
        return fraudDetails;
    }
}
