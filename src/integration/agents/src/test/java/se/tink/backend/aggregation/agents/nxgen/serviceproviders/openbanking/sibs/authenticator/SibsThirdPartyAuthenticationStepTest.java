package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator;

import com.google.common.collect.ImmutableMap;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.ConsentStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class SibsThirdPartyAuthenticationStepTest {

    private ConsentManager consentManager;
    private StrongAuthenticationState strongAuthenticationState;
    private SibsAuthenticator authenticator;
    private SibsThirdPartyAppRequestParamsProvider objectUnderTest;
    private Map<String, String> callbackData = new HashMap<>();
    private AuthenticationRequest authenticationRequest;

    @Before
    public void init() throws MalformedURLException {
        consentManager = Mockito.mock(ConsentManager.class);
        Mockito.when(consentManager.create()).thenReturn(new URL("http://127.0.0.1"));
        strongAuthenticationState = Mockito.mock(StrongAuthenticationState.class);
        authenticator = Mockito.mock(SibsAuthenticator.class);
        objectUnderTest =
                new SibsThirdPartyAppRequestParamsProvider(
                        consentManager, authenticator, strongAuthenticationState);
        callbackData.put("key", "value");
        authenticationRequest = new AuthenticationRequest(Mockito.mock(Credentials.class));
    }

    @Test
    public void shouldHandleSuccessAuthentication()
            throws AuthenticationException, AuthorizationException {
        // given
        Mockito.when(consentManager.getStatus()).thenReturn(ConsentStatus.ACTC);
        authenticationRequest.withCallbackData(ImmutableMap.copyOf(callbackData));
        // when
        objectUnderTest.processThirdPartyCallback(new HashMap<>());
        // then
        Mockito.verify(authenticator).handleManualAuthenticationSuccess();
    }

    @Test(expected = AuthorizationException.class)
    public void shouldHandleFailedAuthenticationWhenConsentStatusIsNotAccepted()
            throws AuthenticationException, AuthorizationException {
        // given
        Mockito.when(consentManager.getStatus()).thenReturn(ConsentStatus.RJCT);
        authenticationRequest.withCallbackData(ImmutableMap.copyOf(callbackData));
        // when
        objectUnderTest.processThirdPartyCallback(new HashMap<>());
        // then
        Mockito.verify(authenticator).handleManualAuthenticationFailure();
    }
}
