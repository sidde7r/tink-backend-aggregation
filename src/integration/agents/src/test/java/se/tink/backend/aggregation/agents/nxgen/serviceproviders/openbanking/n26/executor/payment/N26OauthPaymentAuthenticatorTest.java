package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.executor.payment;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.agentplatform.AgentPlatformHttpClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.N26ProcessStateData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.fetch_authorization_url.N26FetchAuthorizationUrlApiCallParameters;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersProviderConfiguration;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectFetchTokenCallAuthenticationParameters;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AgentHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

@RunWith(MockitoJUnitRunner.class)
public class N26OauthPaymentAuthenticatorTest {

    private static final String CLIENT_ID_FACT = "CLIENT_ID";
    private static final String BASE_URL_FACT = "BASE_URL";
    private static final String REDIRECT_URL_FACT = "REDIRECT_URL";
    private static final String APP_ID_FACT = "APP_ID";
    private N26OauthPaymentAuthenticator n26OauthPaymentAuthenticator;
    private final Xs2aDevelopersProviderConfiguration configuration =
            new Xs2aDevelopersProviderConfiguration(
                    CLIENT_ID_FACT, BASE_URL_FACT, REDIRECT_URL_FACT);
    private final CredentialsRequest credentialsRequest = Mockito.mock(CredentialsRequest.class);
    private final AgentHttpClient agentHttpClient = Mockito.mock(AgentPlatformHttpClient.class);

    @Before
    public void init() {
        when(credentialsRequest.getState()).thenReturn(APP_ID_FACT);
        n26OauthPaymentAuthenticator =
                new N26OauthPaymentAuthenticator(
                        agentHttpClient, configuration, credentialsRequest, new ObjectMapper());
    }

    @Test
    public void testBuildAuthorizeUrlParameter() {
        N26FetchAuthorizationUrlApiCallParameters n26FetchAuthorizationUrlApiCallParameters =
                n26OauthPaymentAuthenticator.prepareAuthorizationParameters(
                        new N26ProcessStateData());
        assertEquals(CLIENT_ID_FACT, n26FetchAuthorizationUrlApiCallParameters.getClientId());
        assertEquals(REDIRECT_URL_FACT, n26FetchAuthorizationUrlApiCallParameters.getRedirectUri());
        assertEquals(APP_ID_FACT, n26FetchAuthorizationUrlApiCallParameters.getState());
    }

    @Test
    public void testAccessTokenParameter() {
        RedirectFetchTokenCallAuthenticationParameters
                redirectFetchTokenCallAuthenticationParameters =
                        n26OauthPaymentAuthenticator.prepareAccessTokenParameter("code");
        assertEquals(
                "code",
                redirectFetchTokenCallAuthenticationParameters
                        .getAgentRemoteInteractionData()
                        .getValue("code"));
        assertEquals(
                APP_ID_FACT,
                redirectFetchTokenCallAuthenticationParameters
                        .getAgentRemoteInteractionData()
                        .getValue("state"));
        assertEquals(
                new HashMap<String, String>(),
                redirectFetchTokenCallAuthenticationParameters
                        .getAuthenticationPersistedData()
                        .valuesCopy());
        assertEquals(
                new HashMap<String, String>(),
                redirectFetchTokenCallAuthenticationParameters
                        .getAuthenticationProcessState()
                        .valuesCopy());
    }

    @Test(expected = IllegalStateException.class)
    public void testNotAllowedPisTokenRefresh() {
        n26OauthPaymentAuthenticator.refreshAccessToken("a-token-key");
    }
}
