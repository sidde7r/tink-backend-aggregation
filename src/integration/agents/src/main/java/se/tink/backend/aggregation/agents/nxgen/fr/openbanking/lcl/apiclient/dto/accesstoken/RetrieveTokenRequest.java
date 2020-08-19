package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.accesstoken;

import lombok.EqualsAndHashCode;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

@EqualsAndHashCode(callSuper = true)
public class RetrieveTokenRequest extends AbstractForm {

    public RetrieveTokenRequest(String clientId, String redirectUri, String code) {
        put("client_id", clientId);
        put("redirect_uri", redirectUri);
        put("grant_type", "authorization_code");
        put("code", code);
    }
}
