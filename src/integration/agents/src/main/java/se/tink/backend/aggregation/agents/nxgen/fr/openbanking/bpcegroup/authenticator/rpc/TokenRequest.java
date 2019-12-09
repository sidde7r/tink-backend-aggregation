package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.BpceGroupConstants.FormKeys;
import se.tink.backend.aggregation.nxgen.http.Form;

public class TokenRequest {
    private final String scope;
    private final String grantType;
    private final String cdetab;
    private final String clientId;
    private final String clientSecret;

    public TokenRequest(
            String scope, String grantType, String cdetab, String clientId, String clientSecret) {
        this.scope = scope;
        this.grantType = grantType;
        this.cdetab = cdetab;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String toData() {
        return Form.builder()
                .put(FormKeys.CLIENT_ID, clientId)
                .put(FormKeys.CLIENT_SECRET, clientSecret)
                .put(FormKeys.GRANT_TYPE, grantType)
                .put(FormKeys.CDETAB, cdetab)
                .put(FormKeys.SCOPE, scope)
                .build()
                .serialize();
    }
}
