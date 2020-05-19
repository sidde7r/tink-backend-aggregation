package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

public class PisTokenRequest extends AbstractForm {

    public PisTokenRequest(String redirect_uri) {
        put(SocieteGeneraleConstants.QueryKeys.REDIRECT_URI, redirect_uri);
        put(
                SocieteGeneraleConstants.QueryKeys.GRANT_TYPE,
                SocieteGeneraleConstants.QueryValues.CLIENT_CREDENTIALS);
        put(
                SocieteGeneraleConstants.QueryKeys.SCOPE,
                SocieteGeneraleConstants.QueryValues.PIS_SCOPE);
    }
}
