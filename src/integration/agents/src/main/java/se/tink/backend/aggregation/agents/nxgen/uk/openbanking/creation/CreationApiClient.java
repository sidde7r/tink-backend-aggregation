package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.creation;

import java.util.Arrays;
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

        return sendTokenRequest(postData).toAccessToken();
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

        return sendTokenRequest(postData).toAccessToken();
    }

    private TokenRequestForm createTokenRequestForm(String grantType, ClientMode mode) {
        WellKnownResponse wellKnownConfiguration = getWellKnownConfiguration();

        String scope =
                wellKnownConfiguration
                        .verifyAndGetScopes(Arrays.asList("openid", mode.getValue()))
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

    private TokenResponse sendTokenRequest(TokenRequestForm postData) {
        TokenResponse tokenResponse = createTokenRequest().body(postData).post(TokenResponse.class);
        log.info(
                "[CREATION CLIENT] From bank response refresh token will expire in {} seconds, but we override it to 89 days",
                tokenResponse.getRefreshExpiresIn());
        tokenResponse.setRefreshExpiresIn(7689600); // 7689600 seconds - equal 89 days
        return tokenResponse;
    }
}
