package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitializationRequest {

    private String serviceName;
    private String version;

    public InitializationRequest(String serviceName, String version) {
        this.serviceName = serviceName;
        this.version = version;
    }

    public static InitializationRequest createAccountServicingRequest() {
        return new InitializationRequest("AccountServicing", "-1");
    }

    public static InitializationRequest createContentRequest() {
        return new InitializationRequest("Content", "-1");
    }
}
