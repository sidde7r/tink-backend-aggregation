package se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.entities.PfmPreferenceWrapper;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchPfmPreferencesResponse {
    private PfmPreferenceWrapper value;

    public PfmPreferenceWrapper getValue() {
        return value;
    }
}
