package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.BredBanquePopulaireConstants;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

public class TokenRequest extends AbstractForm {

    public TokenRequest(String grantType, String code, String redirectUri, String clientId) {
        put(BredBanquePopulaireConstants.QueryKeys.GRANT_TYPE, grantType);
        put(BredBanquePopulaireConstants.QueryKeys.CODE, code);
        put(BredBanquePopulaireConstants.QueryKeys.REDIRECT_URI, redirectUri);
        put(BredBanquePopulaireConstants.QueryKeys.CLIENT_ID, clientId);
    }
}
