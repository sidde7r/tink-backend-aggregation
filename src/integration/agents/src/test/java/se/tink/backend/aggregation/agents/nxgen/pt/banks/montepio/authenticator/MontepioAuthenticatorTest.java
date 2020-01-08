package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.authenticator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.MontepioApiClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class MontepioAuthenticatorTest {

    private CredentialsRequest credentialsRequest;
    private MontepioApiClient apiClient;
    private MontepioAuthenticator objectUnderTest;

    @Before
    public void init() {
        credentialsRequest = Mockito.mock(CredentialsRequest.class);
        apiClient = Mockito.mock(MontepioApiClient.class);
        objectUnderTest = new MontepioAuthenticator(apiClient);
    }

    @Test
    public void isManualAuthenticationShouldReturnTrueWhenCredentialsCreated() {
        // given
        Mockito.when(credentialsRequest.isCreate()).thenReturn(true);
        // when
        boolean manual = objectUnderTest.isManualAuthentication(credentialsRequest);
        // then
        Assert.assertTrue(manual);
    }

    @Test
    public void isManualAuthenticationShouldReturnTrueWhenCredentialsUpdated() {
        // given
        Mockito.when(credentialsRequest.isUpdate()).thenReturn(true);
        // when
        boolean manual = objectUnderTest.isManualAuthentication(credentialsRequest);
        // then
        Assert.assertTrue(manual);
    }

    @Test
    public void
            isManualAuthenticationShouldReturnTrueWhenCredentialsStatusIsOtherThanCreateAndUpdate() {
        // given
        Mockito.when(credentialsRequest.isUpdate()).thenReturn(false);
        Mockito.when(credentialsRequest.isCreate()).thenReturn(false);
        // when
        boolean manual = objectUnderTest.isManualAuthentication(credentialsRequest);
        // then
        Assert.assertFalse(manual);
    }
}
