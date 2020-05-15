package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc;

import java.util.HashMap;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.QueryKeys;

public class TokenRequestGet extends TokenBaseRequest {

    public TokenRequestGet(
            final String clientId,
            final String redirectUri,
            final String code,
            final String grantType) {
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.code = code;
        this.grantType = grantType;
    }

    public Map<String, String> toData() {
        Map<String, String> data = new HashMap<>();
        data.put(QueryKeys.CLIENT_ID, clientId);
        data.put(QueryKeys.REDIRECT_URI, redirectUri);
        data.put(QueryKeys.CODE, code);
        data.put(QueryKeys.GRANT_TYPE, grantType);
        return data;
    }
}
