package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.session;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class KeepAliveRequest {
    private final String token;

    public KeepAliveRequest(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
