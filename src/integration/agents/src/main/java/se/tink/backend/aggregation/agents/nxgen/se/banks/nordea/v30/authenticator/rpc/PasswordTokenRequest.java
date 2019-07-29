package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants.FormParams;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants.TagValues;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.entities.Form;

public class PasswordTokenRequest {
    public static Form of(String codeVerifier, String authorizationCode) {
        Form form = new Form();
        form.put(FormParams.AUTH_METHOD, NordeaSEConstants.AuthMethod.NASA);
        form.put(FormParams.CLIENT_ID, TagValues.APPLICATION_ID);
        form.put(FormParams.COUNTRY, "SE");
        form.put(FormParams.GRANT_TYPE, "authorization_code");
        form.put(FormParams.SCOPE, "ndf");
        form.put(FormParams.REDIRECT_URI, "app://nordeamobile/nasa-auth");
        form.put(NordeaSEConstants.FormParams.CODE, authorizationCode);
        form.put(NordeaSEConstants.FormParams.CODE_VERIFIER, codeVerifier);
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
