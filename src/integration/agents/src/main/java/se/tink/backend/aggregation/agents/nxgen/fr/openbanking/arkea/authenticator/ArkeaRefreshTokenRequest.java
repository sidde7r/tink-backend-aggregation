package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.ArkeaConstants.*;

import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

public class ArkeaRefreshTokenRequest extends AbstractForm {

    public ArkeaRefreshTokenRequest(String grantType, String refreshToken) {
        put(FormKeys.GRANT_TYPE, grantType);
        put(FormKeys.REFRESH_TOKEN, refreshToken);
    }
}
