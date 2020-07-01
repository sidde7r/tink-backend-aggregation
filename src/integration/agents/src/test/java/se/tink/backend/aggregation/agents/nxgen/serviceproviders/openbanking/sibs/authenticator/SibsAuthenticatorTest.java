package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsUserState;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.ConsentLinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SibsAuthenticatorTest {

    private SibsBaseApiClient apiClient;
    private Credentials credentials;
    private SibsUserState userState;
    private StrongAuthenticationState strongAuthenticationState;
    private SibsAuthenticator objectUnderTest;

    @Before
    public void init() {
        apiClient = Mockito.mock(SibsBaseApiClient.class);
        credentials = Mockito.mock(Credentials.class);
        userState = Mockito.mock(SibsUserState.class);
        strongAuthenticationState = Mockito.mock(StrongAuthenticationState.class);
        objectUnderTest =
                new SibsAuthenticator(apiClient, userState, credentials, strongAuthenticationState);
        mockCreateConsentMethod();
    }

    private void mockCreateConsentMethod() {
        ConsentResponse response = Mockito.mock(ConsentResponse.class);
        ConsentLinksEntity links = Mockito.mock(ConsentLinksEntity.class);
        Mockito.when(response.getLinks()).thenReturn(links);
        Mockito.when(links.getRedirect()).thenReturn("http://127.0.0.1");
        Mockito.when(apiClient.createConsent(Mockito.any())).thenReturn(response);
    }

    @Test
    public void isManualAuthenticationShouldReturnTrueWhenConsentInvalid() throws SessionException {
        // given
        Mockito.when(userState.isManualAuthenticationInProgress()).thenReturn(false);
        Mockito.when(apiClient.getConsentStatus())
                .thenThrow(new SessionException(SessionError.SESSION_EXPIRED));
        CredentialsRequest credentialsRequest = Mockito.mock(CredentialsRequest.class);
        // when
        boolean result = objectUnderTest.isManualAuthentication(credentialsRequest);
        // then
        Assert.assertTrue(result);
    }

    @Test
    public void isManualAuthenticationShouldReturnFalseWhenConsentValid() throws SessionException {
        // given
        Mockito.when(userState.isManualAuthenticationInProgress()).thenReturn(false);
        Mockito.when(apiClient.getConsentStatus()).thenReturn(ConsentStatus.ACTC);
        CredentialsRequest credentialsRequest = Mockito.mock(CredentialsRequest.class);
        // when
        boolean result = objectUnderTest.isManualAuthentication(credentialsRequest);
        // then
        Assert.assertFalse(result);
    }

    @Test
    public void isManualAuthenticationShouldReturnTrueWhenManualAuthenticationInProgress()
            throws SessionException {
        // given
        Mockito.when(userState.isManualAuthenticationInProgress()).thenReturn(true);
        Mockito.when(apiClient.getConsentStatus()).thenReturn(ConsentStatus.ACTC);
        CredentialsRequest credentialsRequest = Mockito.mock(CredentialsRequest.class);
        // when
        boolean result = objectUnderTest.isManualAuthentication(credentialsRequest);
        // then
        Assert.assertTrue(result);
    }
}
