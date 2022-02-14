package se.tink.backend.aggregation.agents.nxgen.it.openbanking.cbi.iccrea.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.cbi.iccrea.TestDataReader;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator.IccreaConsentAuthorizationStep;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator.UserInteractions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.CbiConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobeAuthApiClient;

public class IccreaConsentAuthorizationStepTest {

    private static final CbiConsentStatusResponse RECEIVED =
            TestDataReader.readFromFile(
                    TestDataReader.CONSENT_STATUS_RECEIVED, CbiConsentStatusResponse.class);
    private static final CbiConsentStatusResponse REJECTED =
            TestDataReader.readFromFile(
                    TestDataReader.CONSENT_STATUS_REJECTED, CbiConsentStatusResponse.class);
    private static final CbiConsentStatusResponse VALID =
            TestDataReader.readFromFile(
                    TestDataReader.CONSENT_STATUS_VALID, CbiConsentStatusResponse.class);
    private static final CbiConsentStatusResponse CORRUPTED =
            TestDataReader.readFromFile(
                    TestDataReader.CONSENT_STATUS_CORRUPTED, CbiConsentStatusResponse.class);

    private static final int TEST_RETRY_ATTEMPTS = 3;
    private static final String TEST_CONSENT_ID = "1234509876";

    private CbiGlobeAuthApiClient mockAuthApiClient;
    private UserInteractions mockUserInteractions;
    private CbiStorage mockStorage;

    private IccreaConsentAuthorizationStep consentAuthorizationStep;

    @Before
    public void setup() {

        mockAuthApiClient = mock(CbiGlobeAuthApiClient.class);
        mockUserInteractions = mock(UserInteractions.class);
        mockStorage = mock(CbiStorage.class);
        when(mockStorage.getConsentId()).thenReturn(TEST_CONSENT_ID);

        consentAuthorizationStep =
                new IccreaConsentAuthorizationStep(
                        mockAuthApiClient,
                        mockUserInteractions,
                        mockStorage,
                        0,
                        TEST_RETRY_ATTEMPTS);
    }

    @Test
    public void shouldThrowTimedOutWhenNoFinalStatusAfterPolling() {
        // given
        when(mockAuthApiClient.fetchConsentStatus(TEST_CONSENT_ID)).thenReturn(RECEIVED);

        // when
        Throwable throwable = catchThrowable(() -> consentAuthorizationStep.authorizeConsent());

        // then
        verify(mockAuthApiClient, times(TEST_RETRY_ATTEMPTS)).fetchConsentStatus(TEST_CONSENT_ID);
        assertThat(throwable)
                .isInstanceOf(ThirdPartyAppException.class)
                .extracting("error")
                .isEqualTo(ThirdPartyAppError.TIMED_OUT);
    }

    @Test
    public void shouldThrowAuthErrorWhenFinalStatusNotValid() {
        // given
        when(mockAuthApiClient.fetchConsentStatus(TEST_CONSENT_ID))
                .thenReturn(RECEIVED, CORRUPTED, REJECTED);

        // when
        Throwable throwable = catchThrowable(() -> consentAuthorizationStep.authorizeConsent());

        // then
        verify(mockAuthApiClient, times(TEST_RETRY_ATTEMPTS)).fetchConsentStatus(TEST_CONSENT_ID);
        assertThat(throwable)
                .isInstanceOf(ThirdPartyAppException.class)
                .extracting("error")
                .isEqualTo(ThirdPartyAppError.AUTHENTICATION_ERROR);
    }

    @Test
    public void shouldNotThrowAnythingIfFinalStatusIsValid() {
        // given
        when(mockAuthApiClient.fetchConsentStatus(TEST_CONSENT_ID))
                .thenReturn(RECEIVED, RECEIVED, VALID);

        // when & then
        assertThatCode(() -> consentAuthorizationStep.authorizeConsent())
                .doesNotThrowAnyException();
        verify(mockAuthApiClient, times(TEST_RETRY_ATTEMPTS)).fetchConsentStatus(TEST_CONSENT_ID);
    }

    @Test
    public void shouldEndPollingAfterFirstFinalStatus() {
        // given
        when(mockAuthApiClient.fetchConsentStatus(TEST_CONSENT_ID)).thenReturn(RECEIVED, VALID);

        // when & then
        assertThatCode(() -> consentAuthorizationStep.authorizeConsent())
                .doesNotThrowAnyException();
        verify(mockAuthApiClient, times(2)).fetchConsentStatus(TEST_CONSENT_ID);
    }

    @Test
    public void shouldRethrowExceptionInCaseOfAnyDuringRetry() {
        // given
        when(mockAuthApiClient.fetchConsentStatus(TEST_CONSENT_ID))
                .thenReturn(RECEIVED)
                .thenThrow(new RuntimeException("woot"));

        // when
        Throwable throwable = catchThrowable(() -> consentAuthorizationStep.authorizeConsent());

        // then
        verify(mockAuthApiClient, times(2)).fetchConsentStatus(TEST_CONSENT_ID);
        assertThat(throwable).isInstanceOf(RuntimeException.class).hasMessage("woot");
    }
}
