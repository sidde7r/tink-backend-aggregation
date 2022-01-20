package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.capitalone;

import java.util.Collections;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.entities.ClientMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.TokenRequestForm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.WellKnownResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class CapitalOneApiClient extends UkOpenBankingApiClient {

    public CapitalOneApiClient(
            TinkHttpClient httpClient,
            JwtSigner signer,
            SoftwareStatementAssertion softwareStatement,
            String redirectUrl,
            ClientInfo providerConfiguration,
            RandomValueGenerator randomValueGenerator,
            PersistentStorage persistentStorage,
            UkOpenBankingAisConfig aisConfig,
            AgentComponentProvider componentProvider) {
        super(
                httpClient,
                signer,
                softwareStatement,
                redirectUrl,
                providerConfiguration,
                randomValueGenerator,
                persistentStorage,
                aisConfig,
                componentProvider);
    }

    @Override
    public OAuth2Token requestClientCredentials(ClientMode scope) {
        TokenRequestForm postData = createTokenRequestForm("client_credentials", scope);

        return createTokenRequest().body(postData).post(TokenResponse.class).toAccessToken();
    }

    @Override
    public OAuth2Token exchangeAccessCode(String code) {
        TokenRequestForm postData = createTokenRequestFormWithoutScope().withCode(code);

        return createTokenRequest().body(postData).post(TokenResponse.class).toAccessToken();
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken, ClientMode scope) {
        TokenRequestForm postData =
                createTokenRequestForm("refresh_token", scope).withRefreshToken(refreshToken);

        return createTokenRequest().body(postData).post(TokenResponse.class).toAccessToken();
    }

    protected TokenRequestForm createTokenRequestForm(String grantType, ClientMode mode) {
        WellKnownResponse wellKnownConfiguration = getWellKnownConfiguration();
        String scope =
                wellKnownConfiguration
                        .verifyAndGetScopes(Collections.singletonList(mode.getValue()))
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Provider does not support the mandatory scopes."));
        String audience = wellKnownConfiguration.getIssuer();

        return new TokenRequestForm()
                .withGrantType(grantType)
                .withScope(scope)
                .withPrivateKeyJwt(
                        signer,
                        wellKnownConfiguration,
                        providerConfiguration,
                        audience) // Overriding JWT default audience with issuer
                .withRedirectUri(redirectUrl);
    }

    private TokenRequestForm createTokenRequestFormWithoutScope() {
        WellKnownResponse wellKnownConfiguration = getWellKnownConfiguration();
        String audience = wellKnownConfiguration.getIssuer();

        return new TokenRequestForm()
                .withGrantType("authorization_code")
                .withPrivateKeyJwt(
                        signer,
                        wellKnownConfiguration,
                        providerConfiguration,
                        audience) // Overriding JWT default audience with issuer
                .withRedirectUri(redirectUrl);
    }
}
