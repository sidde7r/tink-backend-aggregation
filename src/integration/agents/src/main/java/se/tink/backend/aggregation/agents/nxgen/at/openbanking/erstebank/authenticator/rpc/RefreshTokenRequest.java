package se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.authenticator.rpc;

import java.util.HashMap;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.RefreshTokenBaseRequest;

public class RefreshTokenRequest extends RefreshTokenBaseRequest {

    public RefreshTokenRequest(
            String grantType, String token, String clientId, String clientSecret) {
        super(grantType, token, clientId, clientSecret);
    }

    public Map<String, String> toData() {
        return new HashMap<String, String>() {
            {
                put(QueryKeys.CLIENT_ID, clientId);
                put(QueryKeys.CLIENT_SECRET, clientSecret);
                put(QueryKeys.GRANT_TYPE, grantType);
                put(QueryKeys.REFRESH_TOKEN, token);
            }
        };
    }
}
