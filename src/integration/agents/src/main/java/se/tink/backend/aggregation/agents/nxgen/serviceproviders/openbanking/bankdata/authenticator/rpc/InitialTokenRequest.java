package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants.QueryKeys;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.Form;

@JsonObject
public class InitialTokenRequest {

    @JsonProperty("grant_type")
    private final String grantType;

    @JsonProperty("client_id")
    private final String clientId;

    private final String scope;

    public InitialTokenRequest(String grantType, String scope, String clientId) {
        this.grantType = grantType;
        this.scope = scope;
        this.clientId = clientId;
    }

    public String toData() {
        return Form.builder()
                .put(QueryKeys.GRANT_TYPE, grantType)
                .put(QueryKeys.SCOPE, scope)
                .put(QueryKeys.CLIENT_ID, clientId)
                .build()
                .serialize();
    }
}
