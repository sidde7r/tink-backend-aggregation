package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants.AuthMethod;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants.FormParams;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.entities.Form;

public class PasswordTokenRequest {
    public static Form of(String codeVerifier, String authorizationCode, String clientId) {
        Form form = new Form();
        form.put(FormParams.AUTH_METHOD, AuthMethod.NASA);
        form.put(FormParams.CLIENT_ID, clientId);
        form.put(FormParams.COUNTRY, "SE");
        form.put(FormParams.GRANT_TYPE, "authorization_code");
        form.put(FormParams.SCOPE, "ndf");
        form.put(FormParams.REDIRECT_URI, "app://nordeamobile/nasa-auth");
        form.put(NordeaBaseConstants.FormParams.CODE, authorizationCode);
        form.put(NordeaBaseConstants.FormParams.CODE_VERIFIER, codeVerifier);
        return form;
    }

    public static Form of(String refreshToken) {
        Form form = new Form();
        form.put(FormParams.CLIENT_ID, "NDHMSE");
        form.put(FormParams.GRANT_TYPE, "refresh_token");
        form.put("refresh_token", refreshToken);
        return form;
    }
}
