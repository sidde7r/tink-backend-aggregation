package se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.SantanderConstants.QueryKeys;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.Form;

@JsonObject
public class TokenRequest {
    private final String grantType;

    public TokenRequest(String grantType) {
        this.grantType = grantType;
    }

    public String toData() {
        return Form.builder().put(QueryKeys.GRANT_TYPE, grantType).build().serialize();
    }
}
