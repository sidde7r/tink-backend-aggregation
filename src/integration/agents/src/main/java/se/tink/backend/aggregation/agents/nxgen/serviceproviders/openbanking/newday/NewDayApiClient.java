package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.newday;

import java.util.Arrays;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants.Scopes;
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

public class NewDayApiClient extends UkOpenBankingApiClient {

    public NewDayApiClient(
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

    /*
    Due to lack of refresh_token in the response from NewDay banks (only some part of them).
    Added below fix for checking if we should add "offline_access" to the scope in the request for token.
    This is a temporary solution, I created a task for refactor it below you can find a link:
    https://tinkab.atlassian.net/browse/IFD-3370
     */
    @Override
    public OAuth2Token exchangeAccessCode(String code) {
        TokenRequestForm postData = createAccessTokenRequestForm().withCode(code);

        return createTokenRequest().body(postData).post(TokenResponse.class).toAccessToken();
    }

    private TokenRequestForm createAccessTokenRequestForm() {
        WellKnownResponse wellKnownConfiguration = getWellKnownConfiguration();

        String scope =
                wellKnownConfiguration
                        .verifyAndGetScopes(createScopeList())
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "[NewDayApiClient] Provider does not support the mandatory scopes."));

        TokenRequestForm requestForm =
                new TokenRequestForm()
                        .withGrantType("authorization_code")
                        .withScope(scope)
                        .withRedirectUri(redirectUrl);

        handleFormAuthentication(requestForm, wellKnownConfiguration);

        return requestForm;
    }

    private List<String> createScopeList() {
        return Arrays.asList(ClientMode.ACCOUNTS.getValue(), Scopes.OFFLINE_ACCESS);
    }
}
