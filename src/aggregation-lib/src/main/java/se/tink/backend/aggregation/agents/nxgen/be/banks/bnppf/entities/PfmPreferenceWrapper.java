package se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PfmPreferenceWrapper {
    private PfmPreference pfmPreference;

    public PfmPreference getPfmPreference() {
        return pfmPreference;
    }
}
