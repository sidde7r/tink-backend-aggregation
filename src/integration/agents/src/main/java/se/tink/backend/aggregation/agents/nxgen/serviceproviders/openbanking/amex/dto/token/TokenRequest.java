package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.token;

import lombok.Builder;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

@Builder
public class TokenRequest extends AbstractForm {

    private String redirectUri;
    private String code;
    private String scope;

    private TokenRequest(String redirectUri, String code, String scope) {
        put("scope", scope);
        put("redirect_uri", redirectUri);
        put("grant_type", "authorization_code");
        put("code", code);
    }
}
