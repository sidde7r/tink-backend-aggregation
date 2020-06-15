package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.request;

import lombok.ToString;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

@ToString
public class TokenRequest extends AbstractForm {

    public TokenRequest(String username, String password) {
        put("grant_type", "password");
        put("username", username);
        put("password", password);
        put("scope", "aktiaUser");
    }
}
