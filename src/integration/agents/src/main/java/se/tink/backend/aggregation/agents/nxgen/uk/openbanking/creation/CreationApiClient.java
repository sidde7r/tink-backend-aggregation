package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.creation;

import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
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

/* This class was implemented to bypass the Creation mistakes they return
        information that the refresh token is valid for only 300 seconds.
        When We got information from OpenWorks and founded in the documentation
        of Creation API that the refresh token is valid for 90 days.
        So we decided to overwrite the above value. For more safety reasons
        we will keep refresh token 89 days instead of 90 days as Creation
        informed in the documentation.
        All details you can find in created ticket
        for solving that issue in UK service desk: IFD-2958
        https://tinkab.atlassian.net/browse/IFD-2958

        A refresh token is setting on 7775999 seconds what are equals 90 days without 1 second
*/

@Slf4j
public class CreationApiClient extends UkOpenBankingApiClient {

    public CreationApiClient(
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
    public OAuth2Token refreshAccessToken(String refreshToken, ClientMode scope) {
        TokenRequestForm postData =
                createTokenRequestForm("refresh_token", scope).withRefreshToken(refreshToken);

        return createTokenRequest().body(postData).post(TokenResponse.class).toAccessToken();
    }

    @Override
    public OAuth2Token exchangeAccessCode(String code) {
        TokenRequestForm postData =
                createTokenRequestForm("authorization_code", ClientMode.ACCOUNTS).withCode(code);

        TokenResponse tokenResponse = createTokenRequest().body(postData).post(TokenResponse.class);
        return overrideRefreshExpiresInIfNeeded(tokenResponse).toAccessToken();
    }

    protected TokenRequestForm createTokenRequestForm(String grantType, ClientMode mode) {
        WellKnownResponse wellKnownConfiguration = getWellKnownConfiguration();

        String scope =
                wellKnownConfiguration
                        .verifyAndGetScopes(createScope(grantType, mode))
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Provider does not support the mandatory scopes."));

        TokenRequestForm requestForm =
                new TokenRequestForm()
                        .withGrantType(grantType)
                        .withScope(scope)
                        .withRedirectUri(redirectUrl);

        handleFormAuthentication(requestForm, wellKnownConfiguration);

        return requestForm;
    }

    private TokenResponse overrideRefreshExpiresInIfNeeded(TokenResponse tokenResponse) {
        int refreshExpiresIn = tokenResponse.getRefreshExpiresIn();
        log.info(
                "[CREATION API] Bank declares that refresh token will expire in {} seconds",
                refreshExpiresIn);

        if (refreshExpiresIn < 7775999) {
            tokenResponse.setRefreshExpiresIn(7775999);
            log.info("[CREATION API] Overriding refresh_expires_in with 7775999 sec (90 days)");
        }

        return tokenResponse;
    }

    private List<String> createScope(String grantType, ClientMode mode) {
        if ("client_credentials".equals(grantType)) {
            return Arrays.asList("openid", mode.getValue());
        }

        /**
         * Creation bank requires adding custom scope offline_access to receive refresh token that
         * will expire in 90 day. Without this scope refresh token will live only 5m
         *
         * <p>https://developers.creation.co.uk/images/bnp/documents/User-Guide.pdf (section
         * 5.3.1.1)
         */
        return Arrays.asList("openid", mode.getValue(), "offline_access");
    }
}
