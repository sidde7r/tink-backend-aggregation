package se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UserRegistrationResponse {

    private String installation;

    public String getInstallation() {
        return installation;
    }
}
