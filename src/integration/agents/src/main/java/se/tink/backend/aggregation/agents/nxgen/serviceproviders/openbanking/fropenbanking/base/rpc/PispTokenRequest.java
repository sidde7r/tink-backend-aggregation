package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc;

import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

public class PispTokenRequest extends AbstractForm {

    public PispTokenRequest(String clientId) {
        put("client_id", clientId);
        put("grant_type", "client_credentials");
        put("scope", "pisp");
    }
}
