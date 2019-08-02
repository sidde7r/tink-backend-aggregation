package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.FormKeys;
import se.tink.backend.aggregation.nxgen.http.Form;

public class TokenRequest {

    private final String grantType;
    private final String clientId;
    private final String clientSecret;

    public TokenRequest(String grantType, String clientId, String clientSecret) {
        this.grantType = grantType;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @JsonIgnore
    public String toData() {
        return Form.builder()
                .put(FormKeys.GRANT_TYPE, grantType)
                .put(FormKeys.CLIENT_ID, clientId)
                .put(FormKeys.CLIENT_SECRET, clientSecret)
                .build()
                .serialize();
    }
}
