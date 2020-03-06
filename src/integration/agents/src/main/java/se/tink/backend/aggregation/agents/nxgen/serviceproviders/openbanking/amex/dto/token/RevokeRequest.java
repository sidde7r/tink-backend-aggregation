package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.token;

import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

public class RevokeRequest extends AbstractForm {

    public RevokeRequest(String accessToken) {
        put("grant_type", "revoke");
        put("request_type", "single");
        put("access_token", accessToken);
    }
}
