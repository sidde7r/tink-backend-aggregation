package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSConstants.FormValues.SCOPE_ACCOUNTS;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSConstants.FormKeys;
import se.tink.backend.aggregation.nxgen.http.form.Form;

@RequiredArgsConstructor
public class ICSOAuthTokenFactory {

    private final String clientId;

    private final String clientSecret;

    private final String redirectUri;

    public String clientCredentialsToken() {
        return tokenBuilder()
                .put(FormKeys.SCOPE, SCOPE_ACCOUNTS)
                .put(FormKeys.GRANT_TYPE, ICSOAuthGrantTypes.CLIENT_CREDENTIALS.toString())
                .build()
                .serialize();
    }

    public String consentAuthorizationToken(String authenticationCode) {
        return tokenBuilder()
                .put(FormKeys.SCOPE, SCOPE_ACCOUNTS)
                .put(FormKeys.GRANT_TYPE, ICSOAuthGrantTypes.AUTHORIZATION_CODE.toString())
                .put(FormKeys.AUTHENTICATION_CODE, authenticationCode)
                .put(FormKeys.REDIRECT_URI, redirectUri)
                .build()
                .serialize();
    }

    public String refreshToken(String refreshToken) {
        return tokenBuilder()
                .put(FormKeys.GRANT_TYPE, ICSOAuthGrantTypes.REFRESH_TOKEN.toString())
                .put(FormKeys.REFRESH_TOKEN, refreshToken)
                .build()
                .serialize();
    }

    private Form.Builder tokenBuilder() {
        return Form.builder()
                .put(FormKeys.CLIENT_ID, clientId)
                .put(FormKeys.CLIENT_SECRET, clientSecret);
    }
}
