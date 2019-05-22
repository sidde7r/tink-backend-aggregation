package se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TokenEntity {
    private long id;
    private String created;
    private String updated;
    private String token;

    public long getId() {
        return id;
    }

    public String getCreated() {
        return created;
    }

    public String getUpdated() {
        return updated;
    }

    public String getToken() {
        return token;
    }
}
