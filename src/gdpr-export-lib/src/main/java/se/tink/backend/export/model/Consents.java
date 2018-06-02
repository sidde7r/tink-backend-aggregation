package se.tink.backend.export.model;

import java.util.List;
import se.tink.backend.export.model.submodels.ExportConsent;

public class Consents {

    private final List<ExportConsent> consents;

    public Consents(List<ExportConsent> consents) {
        this.consents = consents;
    }

    public List<ExportConsent> getConsents() {
        return consents;
    }
}
