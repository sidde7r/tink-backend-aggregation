package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ValidateAuthResponse {
    String uak;
    String homeOfficeId;

    public String getUak() {
        return uak;
    }

    public String getHomeOfficeId() {
        return homeOfficeId;
    }
}
