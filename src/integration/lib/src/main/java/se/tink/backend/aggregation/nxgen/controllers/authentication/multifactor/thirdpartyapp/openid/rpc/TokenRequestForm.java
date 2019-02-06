package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatement;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.ClientAssertion;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class TokenRequestForm extends AbstractForm {

    public TokenRequestForm withGrantType(String grantType) {
        this.put("grant_type", grantType);
        return this;
    }

    public TokenRequestForm withScope(String scope) {
        this.put("scope", scope);
        return this;
    }

    public TokenRequestForm withRedirectUri(String redirectUri) {
        this.put("redirect_uri", redirectUri);
        return this;
    }

    public TokenRequestForm withRefreshToken(String refreshToken) {
        this.put("refresh_token", refreshToken);
        return this;
    }

    public TokenRequestForm withCode(String code) {
        this.put("code", code);
        return this;
    }

    public TokenRequestForm withPrivateKeyJwt(SoftwareStatement softwareStatement,
            WellKnownResponse wellknownConfiguration, ClientInfo clientInfo) {
        this.put("client_assertion_type", OpenIdConstants.CLIENT_ASSERTION_TYPE);
        this.put("client_assertion", ClientAssertion.create()
                .withSoftwareStatement(softwareStatement)
                .withWellknownConfiguration(wellknownConfiguration)
                .withClientInfo(clientInfo)
                .build());
        return this;
    }

    public TokenRequestForm withClientSecretPost(String clientId, String clientSecret) {
        this.put("client_id", clientId);
        this.put("client_secret", clientSecret);
        return this;
    }
}
