package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.ArkeaConstants.*;

import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

public class ArkeaGetTokenRequest extends AbstractForm {

    public ArkeaGetTokenRequest(
            String clientId, String grantType, String redirectUri, String code) {
        put(FormKeys.CLIENT_ID, clientId);
        put(FormKeys.GRANT_TYPE, grantType);
        put(FormKeys.REDIRECT_URI, redirectUri);
        put(FormKeys.CODE, code);
    }
}
