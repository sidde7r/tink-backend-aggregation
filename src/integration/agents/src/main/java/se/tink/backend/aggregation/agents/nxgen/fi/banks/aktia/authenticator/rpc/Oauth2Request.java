package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.AktiaConstants;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class Oauth2Request extends AbstractForm {
    public Oauth2Request(String scope, String username, String password) {
        this.put("grant_type", AktiaConstants.HttpParameters.OAUTH2_GRANT_TYPE);
        this.put("scope", scope);
        this.put("username", username);
        this.put("password", password);
    }
}
