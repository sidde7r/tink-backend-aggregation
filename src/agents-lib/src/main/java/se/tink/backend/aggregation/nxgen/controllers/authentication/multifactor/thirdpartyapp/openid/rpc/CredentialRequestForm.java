package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc;

import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class CredentialRequestForm extends AbstractForm {

    public static AbstractForm create(String clientId, String clientSecret) {

        CredentialRequestForm form = new CredentialRequestForm();
        form.put("client_id", clientId);
        form.put("client_secret", clientSecret);
        form.put("grant_type", "client_credentials");
        form.put("scope", "openid accounts");
        return form;
    }
}
