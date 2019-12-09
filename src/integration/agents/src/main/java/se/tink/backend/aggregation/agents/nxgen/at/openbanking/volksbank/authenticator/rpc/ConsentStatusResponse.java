package se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.VolksbankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentStatusResponse {

    private String consentStatus;

    public boolean isValid() {
        return VolksbankConstants.Status.VALID.equalsIgnoreCase(consentStatus);
    }
}
