package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.steps;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import agents_platform_agents_framework.org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDate;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.SebBalticsApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.SebBalticsConstants.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.SebBalticsDecoupledAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

@RunWith(JUnitParamsRunner.class)
public class CreateNewConsentStepTest {

    private CreateNewConsentStep createNewConsentStep;
    private SebBalticsApiClient apiClient;
    private ConsentResponse consentResponse;

    @Before
    public void setUp() {
        SebBalticsDecoupledAuthenticator authenticator =
                mock(SebBalticsDecoupledAuthenticator.class);
        PersistentStorage persistentStorage = mock(PersistentStorage.class);
        CredentialsRequest credentialsRequest = mock(CredentialsRequest.class);
        LocalDate localDate = LocalDate.now();
        apiClient = mock(SebBalticsApiClient.class);
        createNewConsentStep =
                new CreateNewConsentStep(
                        authenticator, apiClient, persistentStorage, credentialsRequest, localDate);
        consentResponse = mock(ConsentResponse.class);
    }

    @Test
    public void shoudNotCheckStatusAgainWhenStatusValid() {
        // when
        Mockito.when(apiClient.getConsentStatus(Mockito.anyString())).thenReturn(consentResponse);
        Mockito.when(consentResponse.getConsentStatus()).thenReturn(ConsentStatus.VALID);
        ReflectionTestUtils.invokeMethod(createNewConsentStep, "poll", "consentId");

        // then
        verify(apiClient, times(1)).getConsentStatus(Mockito.anyString());
    }

    @Test
    public void shouldCheckStatusAgainWhenStatusReceived() {
        // when
        Mockito.when(apiClient.getConsentStatus(Mockito.anyString())).thenReturn(consentResponse);
        Mockito.when(consentResponse.getConsentStatus())
                .thenReturn(ConsentStatus.RECEIVED, ConsentStatus.RECEIVED, ConsentStatus.VALID);
        ReflectionTestUtils.invokeMethod(createNewConsentStep, "poll", "consentId");

        // then
        verify(apiClient, times(3)).getConsentStatus(Mockito.anyString());
    }

    @Test
    @Parameters(method = "consentStatusAndErrorDescription")
    public void shouldThrowException(String consentStatus, String exceptionMessage) {
        // when
        Mockito.when(apiClient.getConsentStatus(Mockito.anyString())).thenReturn(consentResponse);
        Mockito.when(consentResponse.getConsentStatus()).thenReturn(consentStatus);

        // then
        assertThatThrownBy(
                        () ->
                                ReflectionTestUtils.invokeMethod(
                                        createNewConsentStep, "poll", "consentId"))
                .isInstanceOf(ThirdPartyAppError.AUTHENTICATION_ERROR.exception().getClass())
                .hasMessage(exceptionMessage);
    }

    private Object[] consentStatusAndErrorDescription() {
        return new Object[] {
            new Object[] {ConsentStatus.REJECTED, "Consent rejected by user"},
            new Object[] {ConsentStatus.REVOKED_BY_PSU, "Consent revoked by PSU"},
            new Object[] {ConsentStatus.EXPIRED, "Consent expired"},
            new Object[] {ConsentStatus.TERMINATED_BY_TPP, "Consent terminated by TPP"},
            new Object[] {"", "Unknown status"}
        };
    }
}
