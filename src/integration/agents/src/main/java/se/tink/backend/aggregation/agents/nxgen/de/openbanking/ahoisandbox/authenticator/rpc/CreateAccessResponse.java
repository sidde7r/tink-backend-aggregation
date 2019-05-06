package se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreateAccessResponse {

    private String type;
    private String id;
    private String providerId;
    private String validationState;

    public String getId() {
        return id;
    }
}
