package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class RefreshTokenForm extends AbstractForm {
    private RefreshTokenForm(String refreshToken) {
        put(NordeaBaseConstants.FormKeys.GRANT_TYPE, NordeaBaseConstants.FormValues.REFRESH_TOKEN);
        put(NordeaBaseConstants.FormKeys.REFRESH_TOKEN, refreshToken);
    }

    public static RefreshTokenForm of(String refreshToken) {
        return new RefreshTokenForm(refreshToken);
    }
}
