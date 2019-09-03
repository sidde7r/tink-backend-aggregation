package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants.QueryValues;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.Form;

@JsonObject
public class TokenRequest {
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final String code;

    public TokenRequest(String clientId, String clientSecret, String redirectUri, String code) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.code = code;
    }

    public String toData() {
        return Form.builder()
                .put(SwedbankConstants.QueryKeys.CLIENT_ID, clientId)
                .put(SwedbankConstants.QueryKeys.CLIENT_SECRET, clientSecret)
                .put(SwedbankConstants.QueryKeys.REDIRECT_URI, redirectUri)
                .put(SwedbankConstants.QueryKeys.CODE, code)
                .put(SwedbankConstants.QueryKeys.GRANT_TYPE, QueryValues.GRANT_TYPE_CODE)
                .build()
                .serialize();
    }
}
