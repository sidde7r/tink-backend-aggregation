package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConsentDetailsResponse {
    private String validUntil;

    public String getValidUntil() {
        return validUntil;
    }
}
