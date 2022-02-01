package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.BredBanquePopulaireConstants;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

public class RefreshRequest extends AbstractForm {
    public RefreshRequest(String clientId, String refreshToken) {
        put(BredBanquePopulaireConstants.QueryKeys.CLIENT_ID, clientId);
        put(
                BredBanquePopulaireConstants.QueryKeys.GRANT_TYPE,
                BredBanquePopulaireConstants.QueryValues.REFRESH_TOKEN);
        put(BredBanquePopulaireConstants.QueryKeys.REFRESH_TOKEN, refreshToken);
    }
}
