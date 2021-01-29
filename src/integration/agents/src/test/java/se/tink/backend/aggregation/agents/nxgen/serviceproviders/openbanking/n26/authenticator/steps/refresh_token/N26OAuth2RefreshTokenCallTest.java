package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.refresh_token;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.N26BaseApiCallTest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectRefreshTokenCallAuthenticationParameters;

@RunWith(MockitoJUnitRunner.class)
public class N26OAuth2RefreshTokenCallTest extends N26BaseApiCallTest {
    private N26OAuth2RefreshTokenCall n26OAuth2RefreshTokenCall;

    @Before
    public void init() {
        N26RefreshTokenParameters parameters =
                N26RefreshTokenParameters.builder().baseUrl(BASE_URL).clientId(CLIENT_ID).build();
        n26OAuth2RefreshTokenCall = new N26OAuth2RefreshTokenCall(agentHttpClient, parameters);
    }

    @Test
    public void testRefreshTokenEndpointUri() {
        assertEquals(
                n26OAuth2RefreshTokenCall.getRefreshTokenEndpoint(),
                URI.create(BASE_URL + "/oauth2/token?role=DEDICATED_AISP"));
    }

    @Test
    public void testClientInfo() {
        RedirectRefreshTokenCallAuthenticationParameters mock =
                Mockito.mock(RedirectRefreshTokenCallAuthenticationParameters.class);
        assertEquals(n26OAuth2RefreshTokenCall.getClientSpecificHeaders(mock), new HashMap<>());
        assertEquals(n26OAuth2RefreshTokenCall.getClientSpecificParams(mock), new HashMap<>());
        assertEquals(n26OAuth2RefreshTokenCall.getClientId(mock), CLIENT_ID);
    }
}
