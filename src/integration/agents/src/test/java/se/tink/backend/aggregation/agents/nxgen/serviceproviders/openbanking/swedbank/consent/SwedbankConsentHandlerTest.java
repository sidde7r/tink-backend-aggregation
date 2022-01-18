package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.consent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import agents_platform_agents_framework.org.springframework.test.util.ReflectionTestUtils;
import javax.ws.rs.core.MediaType;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.resources.GenericResponseTestData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.rpc.GenericResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RunWith(MockitoJUnitRunner.class)
public class SwedbankConsentHandlerTest {
    private SwedbankConsentHandler objectUnderTest;
    private SwedbankApiClient apiClient;
    private PersistentStorage persistentStorage;
    @Mock private HttpResponse httpResponse;
    @Mock private HttpResponseException httpResponseException;

    @Before
    public void setUp() {
        persistentStorage = new PersistentStorage();
        apiClient = mock(SwedbankApiClient.class);
        objectUnderTest = new SwedbankConsentHandler(apiClient, persistentStorage);
    }

    @Test
    public void shouldStoreValidAllAccountsConsent() {
        // given
        when(apiClient.getConsentAllAccounts())
                .thenReturn(SwedbankConsentHandlerTestData.VALID_CONSENT_RESPONSE);

        // when
        objectUnderTest.getAndStoreConsentForAllAccounts();

        // then
        assertThat(persistentStorage.get(StorageKeys.CONSENT)).isEqualTo("consentId123");
    }

    @Test
    public void shouldThrowIllegalStateExceptionIfAllAccountsConsentIsNotValid() {
        // given
        when(apiClient.getConsentAllAccounts())
                .thenReturn(SwedbankConsentHandlerTestData.INVALID_CONSENT_RESPONSE);

        // when
        final ThrowingCallable callable = () -> objectUnderTest.getAndStoreConsentForAllAccounts();

        // then
        assertThatThrownBy(callable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "All accounts consent status was not valid. "
                                + "It's expected to be valid, this needs to be investigated.");

        assertNull(persistentStorage.get(StorageKeys.CONSENT));
    }

    @Test
    public void shouldStoreValidAccountDetailsConsent() {
        // given
        ReflectionTestUtils.setField(
                objectUnderTest,
                "fetchAccountResponse",
                SwedbankConsentHandlerTestData.ACCOUNTS_RESPONSE);
        when(apiClient.getConsentAccountDetails(any()))
                .thenReturn(SwedbankConsentHandlerTestData.VALID_CONSENT_RESPONSE);

        // when
        objectUnderTest.getAndStoreDetailedConsent();

        // then
        assertThat(persistentStorage.get(StorageKeys.CONSENT)).isEqualTo("consentId123");
    }

    @Test
    public void shouldRemoveConsentAndReThrowIfInvalidConsentWhenFetchingAccounts() {
        // given
        persistentStorage.put(StorageKeys.CONSENT, "consentId123");

        GenericResponse genericResponse = mock(GenericResponse.class);
        when(genericResponse.isConsentInvalid()).thenReturn(true);

        when(httpResponse.getType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);
        when(httpResponse.getBody(GenericResponse.class)).thenReturn(genericResponse);
        when(httpResponseException.getResponse()).thenReturn(httpResponse);
        when(apiClient.fetchAccounts()).thenThrow(httpResponseException);

        // when
        final ThrowingCallable callable = () -> objectUnderTest.getListOfAccounts();

        // then
        assertThatThrownBy(callable).isInstanceOf(HttpResponseException.class);
        assertNull(persistentStorage.get(StorageKeys.CONSENT));
    }

    @Test
    public void shouldThrowAuthorizationErrorIfInvalidKycWhenFetchingAccounts() {
        // given
        when(httpResponse.getType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);
        when(httpResponse.getBody(GenericResponse.class))
                .thenReturn(GenericResponseTestData.INVALID_KYC);
        when(httpResponseException.getResponse()).thenReturn(httpResponse);
        when(apiClient.fetchAccounts()).thenThrow(httpResponseException);

        // when
        final ThrowingCallable callable = () -> objectUnderTest.getListOfAccounts();

        // then
        assertThatThrownBy(callable)
                .isInstanceOf(AuthorizationException.class)
                .hasMessage("Cause: AuthorizationError.ACCOUNT_BLOCKED");
        ;
    }

    @Test
    public void shouldThrowAuthorizationErrorIfMissingAgreementWhenFetchingAccounts() {
        // given
        when(httpResponse.getType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);
        when(httpResponse.getBody(GenericResponse.class))
                .thenReturn(GenericResponseTestData.INTERNET_BANK_AGREEMENT);
        when(httpResponseException.getResponse()).thenReturn(httpResponse);
        when(apiClient.fetchAccounts()).thenThrow(httpResponseException);

        // when
        final ThrowingCallable callable = () -> objectUnderTest.getListOfAccounts();

        // then
        assertThatThrownBy(callable).isInstanceOf(AuthorizationException.class);
    }

    @Test
    public void shouldThrowLoginErrorIfNoAccountsReceivedWhenFetchingAccounts() {
        // given
        when(apiClient.fetchAccounts())
                .thenReturn(SwedbankConsentHandlerTestData.EMPTY_ACCOUNTS_RESPONSE);

        // when
        final ThrowingCallable callable = () -> objectUnderTest.getListOfAccounts();

        // then
        assertThatThrownBy(callable)
                .isInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.NO_ACCOUNTS");
    }

    @Test
    public void shouldThrowIllegalStateExceptionIfAccountDetailsConsentIsInvalid() {
        // given
        ReflectionTestUtils.setField(
                objectUnderTest,
                "fetchAccountResponse",
                SwedbankConsentHandlerTestData.ACCOUNTS_RESPONSE);

        when(apiClient.getConsentAccountDetails(any()))
                .thenReturn(SwedbankConsentHandlerTestData.INVALID_CONSENT_RESPONSE);

        // when
        final ThrowingCallable callable = () -> objectUnderTest.getAndStoreDetailedConsent();

        // then
        assertThatThrownBy(callable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "Account details consent status was not valid. "
                                + "It's expected to be valid, this needs to be investigated.");

        assertNull(persistentStorage.get(StorageKeys.CONSENT));
    }

    @Test
    public void shouldNotThrowIfConsentIsValidWhenVerifyingConsent() {
        // given
        persistentStorage.put(StorageKeys.CONSENT, "consentId123");
        when(apiClient.getConsentStatus(any())).thenReturn("valid");

        // when
        final Throwable thrown = catchThrowable(() -> objectUnderTest.verifyValidConsentOrThrow());

        // then
        assertThat(thrown).isNull();
    }

    @Test
    public void shouldThrowSessionErrorIfConsentHasExpired() {
        // given
        persistentStorage.put(StorageKeys.CONSENT, "consentId123");
        when(apiClient.getConsentStatus(any())).thenReturn("expired");

        // when
        final ThrowingCallable callable = () -> objectUnderTest.verifyValidConsentOrThrow();

        // then
        assertThatThrownBy(callable)
                .isInstanceOf(SessionException.class)
                .hasMessage("Cause: SessionError.CONSENT_EXPIRED");
    }

    @Test
    public void shouldThrowSessionErrorIfConsentHasBeenRevokedByPsu() {
        // given
        persistentStorage.put(StorageKeys.CONSENT, "consentId123");
        when(apiClient.getConsentStatus(any())).thenReturn("revokedByPsu");

        // when
        final ThrowingCallable callable = () -> objectUnderTest.verifyValidConsentOrThrow();

        // then
        assertThatThrownBy(callable)
                .isInstanceOf(SessionException.class)
                .hasMessage("Cause: SessionError.CONSENT_REVOKED_BY_USER");
    }

    @Test
    public void shouldThrowSessionErrorIfConsentHasBeenTerminatedByTpp() {
        // given
        persistentStorage.put(StorageKeys.CONSENT, "consentId123");
        when(apiClient.getConsentStatus(any())).thenReturn("terminatedByTpp");

        // when
        final ThrowingCallable callable = () -> objectUnderTest.verifyValidConsentOrThrow();

        // then
        assertThatThrownBy(callable)
                .isInstanceOf(SessionException.class)
                .hasMessage("Cause: SessionError.CONSENT_INVALID");
    }

    @Test
    public void shouldThrowIllegalStateExceptionOnUnknownConsentStatusWhenVerifyingConsent() {
        // given
        persistentStorage.put(StorageKeys.CONSENT, "consentId123");
        when(apiClient.getConsentStatus(any())).thenReturn("unknownConsentStatus");

        // when
        final ThrowingCallable callable = () -> objectUnderTest.verifyValidConsentOrThrow();

        // then
        assertThatThrownBy(callable).isInstanceOf(IllegalStateException.class);
    }
}
