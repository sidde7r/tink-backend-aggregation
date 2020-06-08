package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentDetailsResponse {

    private String validUntil;

    public String getValidUntil() {
        return validUntil;
    }
}
