package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PayloadEntity {
    private String response;
    private String signedResponse;

    public String getResponse() {
        return response;
    }

    public String getSignedResponse() {
        return signedResponse;
    }
}
