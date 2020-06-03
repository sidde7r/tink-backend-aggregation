package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.payment;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

public class PispTokenRequest extends AbstractForm {

    PispTokenRequest(String clientId) {
        put(BnpParibasBaseConstants.QueryKeys.CLIENT_ID, clientId);
        put(BnpParibasBaseConstants.QueryKeys.GRANT_TYPE, "client_credentials");
        put(BnpParibasBaseConstants.QueryKeys.SCOPE, "pisp");
    }
}
