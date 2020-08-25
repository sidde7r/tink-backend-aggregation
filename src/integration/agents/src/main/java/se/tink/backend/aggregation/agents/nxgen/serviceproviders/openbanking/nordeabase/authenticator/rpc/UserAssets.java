package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UserAssets {
    private String[] scopes;

    public String[] getScopes() {
        return scopes;
    }
}
