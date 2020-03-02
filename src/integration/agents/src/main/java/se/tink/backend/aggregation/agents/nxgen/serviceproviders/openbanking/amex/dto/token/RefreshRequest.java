package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.token;

import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

public class RefreshRequest extends AbstractForm {

    public RefreshRequest(String refreshToken) {
        put("grant_type", "refresh_token");
        put("refresh_token", refreshToken);
    }
}
