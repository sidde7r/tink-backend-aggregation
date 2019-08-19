package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.QueryKeys;
import se.tink.backend.aggregation.nxgen.http.Form;

public class TokenRequest {

    private String scope;
    private String code;
    private String grantType;
    private String redirectUri;

    public TokenRequest(String scope, String code, String grantType, String redirectUri) {
        this.scope = scope;
        this.code = code;
        this.grantType = grantType;
        this.redirectUri = redirectUri;
    }

    public String toData() {
        return Form.builder()
                .put(QueryKeys.SCOPE, scope)
                .put(QueryKeys.CODE, code)
                .put(QueryKeys.GRANT_TYPE, grantType)
                .put(QueryKeys.REDIRECT_URI, redirectUri)
                .build()
                .serialize();
    }
}
