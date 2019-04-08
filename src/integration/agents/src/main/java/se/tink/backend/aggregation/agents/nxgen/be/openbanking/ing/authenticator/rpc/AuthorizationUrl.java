package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthorizationUrl {

    private String location;

    public String getLocation() {
        return location;
    }
}
