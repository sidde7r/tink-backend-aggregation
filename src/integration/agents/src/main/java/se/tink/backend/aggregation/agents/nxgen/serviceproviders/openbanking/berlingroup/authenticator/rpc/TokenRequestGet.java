package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc;

import java.util.HashMap;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.QueryKeys;

public class TokenRequestGet extends TokenBaseRequest {

    public TokenRequestGet(
            String clientId,
            String clientSecret,
            String redirectUri,
            String code,
            String grantType) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.code = code;
        this.grantType = grantType;
    }

    public Map<String, String> toData() {
        return new HashMap<String, String>() {
            {
                put(QueryKeys.CLIENT_ID, clientId);
                put(QueryKeys.CLIENT_SECRET, clientSecret);
                put(QueryKeys.REDIRECT_URI, redirectUri);
                put(QueryKeys.CODE, code);
                put(QueryKeys.GRANT_TYPE, grantType);
            }
        };
    }
}
