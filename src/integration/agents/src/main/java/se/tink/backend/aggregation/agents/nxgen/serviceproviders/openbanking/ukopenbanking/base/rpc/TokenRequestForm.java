package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.rpc;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingV31Constants.CLIENT_ASSERTION_TYPE;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.configuration.ClientInfo;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.jwt.ClientAssertion;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.jwt.signer.iface.JwtSigner.Algorithm;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

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

    public TokenRequestForm withPrivateKeyJwt(
            JwtSigner signer, WellKnownResponse wellKnownConfiguration, ClientInfo clientInfo) {

        JwtSigner.Algorithm signingAlg;
        if (Strings.isNullOrEmpty(clientInfo.getTokenEndpointAuthSigningAlg())) {
            signingAlg = Algorithm.RS256;
        } else {
            signingAlg = JwtSigner.Algorithm.valueOf(clientInfo.getTokenEndpointAuthSigningAlg());
        }

        String clientAssertion =
                ClientAssertion.create()
                        .withWellKnownConfiguration(wellKnownConfiguration)
                        .withClientInfo(clientInfo)
                        .build(signer, signingAlg);

        this.withClientId(clientInfo.getClientId());
        this.put("client_assertion_type", CLIENT_ASSERTION_TYPE);
        this.put("client_assertion", clientAssertion);
        return this;
    }

    public TokenRequestForm withClientSecretPost(String clientId, String clientSecret) {
        this.withClientId(clientId);
        this.put("client_secret", clientSecret);
        return this;
    }

    public TokenRequestForm withClientId(String clientId) {
        this.put("client_id", clientId);
        return this;
    }
}
