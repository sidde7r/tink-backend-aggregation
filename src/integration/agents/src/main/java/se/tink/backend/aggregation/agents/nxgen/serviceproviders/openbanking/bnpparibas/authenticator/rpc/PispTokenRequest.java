package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

public class PispTokenRequest extends AbstractForm {

    public PispTokenRequest(String clientId, String grantType, String scope) {
        put(BnpParibasBaseConstants.QueryKeys.CLIENT_ID, clientId);
        put(BnpParibasBaseConstants.QueryKeys.GRANT_TYPE, grantType);
        put(BnpParibasBaseConstants.QueryKeys.SCOPE, scope);
    }
}
