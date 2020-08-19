package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.accesstoken;

import lombok.EqualsAndHashCode;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

@EqualsAndHashCode(callSuper = true)
public class RefreshTokenRequest extends AbstractForm {

    public RefreshTokenRequest(String clientId, String refresh_token) {
        put("client_id", clientId);
        put("scope", "aisp");
        put("grant_type", "refresh_token");
        put("refresh_token", refresh_token);
    }
}
