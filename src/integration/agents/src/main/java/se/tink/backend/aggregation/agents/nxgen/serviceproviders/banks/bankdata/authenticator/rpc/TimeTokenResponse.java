package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TimeTokenResponse {
    private String timeToken;

    public String getTimeToken() {
        return timeToken;
    }
}
