package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.AUTHORIZE_URL;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.AUTH_CODE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.CALLBACK_URL;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.CONSENT_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.STATE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.SUPPLEMENTAL_KEY;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createCallbackDataWithErrorAndDescription;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createCallbackDataWithErrorAndNoDescription;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createCallbackDataWithNoCode;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createClientInfo;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createCorrectCallbackData;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createStrongAuthenticationState;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.authenticator.UkOpenBankingPaymentAuthenticator.WAIT_FOR_MINUTES;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationCancelledByUserException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationFailedByUserException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationTimeOutException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticationValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class UkOpenBankingPaymentAuthenticatorTest {

    private UkOpenBankingPaymentAuthenticator ukOpenBankingPaymentAuthenticator;

    private UkOpenBankingAuthenticationErrorMatcher authenticationErrorMatcherMock;
    private SupplementalInformationHelper supplementalInformationHelperMock;

    @Before
    public void setUp() {
        final ClientInfo clientInfoMock = createClientInfo();
        final UkOpenBankingPaymentApiClient apiClientMock = createApiClientMock(clientInfoMock);
        final OpenIdAuthenticationValidator authenticationValidatorMock =
                mock(OpenIdAuthenticationValidator.class);
        final StrongAuthenticationState strongAuthenticationStateMock =
                createStrongAuthenticationState();

        authenticationErrorMatcherMock = mock(UkOpenBankingAuthenticationErrorMatcher.class);
        supplementalInformationHelperMock = mock(SupplementalInformationHelper.class);
        ukOpenBankingPaymentAuthenticator =
                new UkOpenBankingPaymentAuthenticator(
                        apiClientMock,
                        authenticationValidatorMock,
                        authenticationErrorMatcherMock,
                        strongAuthenticationStateMock,
                        supplementalInformationHelperMock,
                        CALLBACK_URL,
                        clientInfoMock);
    }

    @Test
    public void shouldAuthenticate() throws PaymentAuthorizationException {
        // given
        final Map<String, String> callbackData = createCorrectCallbackData();
        when(supplementalInformationHelperMock.waitForSupplementalInformation(
                        SUPPLEMENTAL_KEY, WAIT_FOR_MINUTES, TimeUnit.MINUTES))
                .thenReturn(Optional.of(callbackData));

        // when
        final String returned = ukOpenBankingPaymentAuthenticator.authenticate(CONSENT_ID);

        // then
        assertThat(returned).isEqualTo(AUTH_CODE);
        verifyThirdPartyAppWasOpened();
    }

    @Test
    public void shouldThrowExceptionWhenSupplementalRequestTimedOut() {
        // given
        when(supplementalInformationHelperMock.waitForSupplementalInformation(
                        SUPPLEMENTAL_KEY, WAIT_FOR_MINUTES, TimeUnit.MINUTES))
                .thenThrow(ThirdPartyAppError.TIMED_OUT.exception());

        // when
        final Throwable thrown =
                catchThrowable(() -> ukOpenBankingPaymentAuthenticator.authenticate(CONSENT_ID));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(PaymentAuthorizationTimeOutException.class)
                .hasNoCause()
                .hasMessage(PaymentAuthorizationTimeOutException.MESSAGE);

        verifyThirdPartyAppWasOpened();
    }

    @Test
    public void shouldThrowExceptionWhenSupplementalResponseContainsKnownErrorAndNoDescription() {
        // given
        final Map<String, String> callbackData = createCallbackDataWithErrorAndNoDescription();
        when(supplementalInformationHelperMock.waitForSupplementalInformation(
                        SUPPLEMENTAL_KEY, WAIT_FOR_MINUTES, TimeUnit.MINUTES))
                .thenReturn(Optional.of(callbackData));

        when(authenticationErrorMatcherMock.isKnownOpenIdError(anyString()))
                .thenReturn(Boolean.TRUE);

        // when
        final Throwable thrown =
                catchThrowable(() -> ukOpenBankingPaymentAuthenticator.authenticate(CONSENT_ID));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(PaymentAuthorizationException.class)
                .hasNoCause()
                .hasMessage(PaymentAuthorizationException.DEFAULT_MESSAGE);

        verifyThirdPartyAppWasOpened();
    }

    @Test
    public void shouldThrowExceptionWhenSupplementalResponseContainsKnownErrorCancelledByUser() {
        // given
        final Map<String, String> callbackData = createCallbackDataWithErrorAndDescription();
        when(supplementalInformationHelperMock.waitForSupplementalInformation(
                        SUPPLEMENTAL_KEY, WAIT_FOR_MINUTES, TimeUnit.MINUTES))
                .thenReturn(Optional.of(callbackData));

        when(authenticationErrorMatcherMock.isKnownOpenIdError(anyString()))
                .thenReturn(Boolean.TRUE);
        when(authenticationErrorMatcherMock.isAuthorizationCancelledByUser(anyString()))
                .thenReturn(Boolean.TRUE);
        when(authenticationErrorMatcherMock.isAuthorizationTimeOut(anyString()))
                .thenReturn(Boolean.FALSE);
        when(authenticationErrorMatcherMock.isAuthorizationFailedByUser(anyString()))
                .thenReturn(Boolean.FALSE);

        // when
        final Throwable thrown =
                catchThrowable(() -> ukOpenBankingPaymentAuthenticator.authenticate(CONSENT_ID));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(PaymentAuthorizationCancelledByUserException.class)
                .hasNoCause()
                .hasMessage(PaymentAuthorizationCancelledByUserException.MESSAGE);

        verifyThirdPartyAppWasOpened();
    }

    @Test
    public void shouldThrowExceptionWhenSupplementalResponseContainsKnownErrorTimeout() {
        // given
        final Map<String, String> callbackData = createCallbackDataWithErrorAndDescription();
        when(supplementalInformationHelperMock.waitForSupplementalInformation(
                        SUPPLEMENTAL_KEY, WAIT_FOR_MINUTES, TimeUnit.MINUTES))
                .thenReturn(Optional.of(callbackData));

        when(authenticationErrorMatcherMock.isKnownOpenIdError(anyString()))
                .thenReturn(Boolean.TRUE);
        when(authenticationErrorMatcherMock.isAuthorizationCancelledByUser(anyString()))
                .thenReturn(Boolean.FALSE);
        when(authenticationErrorMatcherMock.isAuthorizationTimeOut(anyString()))
                .thenReturn(Boolean.TRUE);
        when(authenticationErrorMatcherMock.isAuthorizationFailedByUser(anyString()))
                .thenReturn(Boolean.FALSE);

        // when
        final Throwable thrown =
                catchThrowable(() -> ukOpenBankingPaymentAuthenticator.authenticate(CONSENT_ID));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(PaymentAuthorizationTimeOutException.class)
                .hasNoCause()
                .hasMessage(PaymentAuthorizationTimeOutException.MESSAGE);

        verifyThirdPartyAppWasOpened();
    }

    @Test
    public void shouldThrowExceptionWhenSupplementalResponseContainsKnownErrorFailedByUser() {
        // given
        final Map<String, String> callbackData = createCallbackDataWithErrorAndDescription();
        when(supplementalInformationHelperMock.waitForSupplementalInformation(
                        SUPPLEMENTAL_KEY, WAIT_FOR_MINUTES, TimeUnit.MINUTES))
                .thenReturn(Optional.of(callbackData));

        when(authenticationErrorMatcherMock.isKnownOpenIdError(anyString()))
                .thenReturn(Boolean.TRUE);
        when(authenticationErrorMatcherMock.isAuthorizationCancelledByUser(anyString()))
                .thenReturn(Boolean.FALSE);
        when(authenticationErrorMatcherMock.isAuthorizationTimeOut(anyString()))
                .thenReturn(Boolean.FALSE);
        when(authenticationErrorMatcherMock.isAuthorizationFailedByUser(anyString()))
                .thenReturn(Boolean.TRUE);

        // when
        final Throwable thrown =
                catchThrowable(() -> ukOpenBankingPaymentAuthenticator.authenticate(CONSENT_ID));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(PaymentAuthorizationFailedByUserException.class)
                .hasNoCause()
                .hasMessage(PaymentAuthorizationFailedByUserException.MESSAGE);

        verifyThirdPartyAppWasOpened();
    }

    @Test
    public void shouldThrowExceptionWhenSupplementalResponseContainsKnownErrorAndUnknownMessage() {
        // given
        final Map<String, String> callbackData = createCallbackDataWithErrorAndDescription();
        when(supplementalInformationHelperMock.waitForSupplementalInformation(
                        SUPPLEMENTAL_KEY, WAIT_FOR_MINUTES, TimeUnit.MINUTES))
                .thenReturn(Optional.of(callbackData));

        when(authenticationErrorMatcherMock.isKnownOpenIdError(anyString()))
                .thenReturn(Boolean.TRUE);
        when(authenticationErrorMatcherMock.isAuthorizationCancelledByUser(anyString()))
                .thenReturn(Boolean.FALSE);
        when(authenticationErrorMatcherMock.isAuthorizationTimeOut(anyString()))
                .thenReturn(Boolean.FALSE);
        when(authenticationErrorMatcherMock.isAuthorizationFailedByUser(anyString()))
                .thenReturn(Boolean.FALSE);

        // when
        final Throwable thrown =
                catchThrowable(() -> ukOpenBankingPaymentAuthenticator.authenticate(CONSENT_ID));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(PaymentAuthorizationException.class)
                .hasNoCause()
                .hasMessage(PaymentAuthorizationException.DEFAULT_MESSAGE);

        verifyThirdPartyAppWasOpened();
    }

    @Test
    public void shouldThrowExceptionWhenSupplementalResponseContainsUnknownError() {
        // given
        final Map<String, String> callbackData = createCallbackDataWithErrorAndDescription();
        when(supplementalInformationHelperMock.waitForSupplementalInformation(
                        SUPPLEMENTAL_KEY, WAIT_FOR_MINUTES, TimeUnit.MINUTES))
                .thenReturn(Optional.of(callbackData));

        when(authenticationErrorMatcherMock.isKnownOpenIdError(anyString()))
                .thenReturn(Boolean.FALSE);

        // when
        final Throwable thrown =
                catchThrowable(() -> ukOpenBankingPaymentAuthenticator.authenticate(CONSENT_ID));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasNoCause()
                .hasMessageStartingWith("Unknown error:");

        verifyThirdPartyAppWasOpened();
    }

    @Test
    public void shouldThrowExceptionWhenSupplementalResponseDoesNotContainCode() {
        // given
        final Map<String, String> callbackData = createCallbackDataWithNoCode();
        when(supplementalInformationHelperMock.waitForSupplementalInformation(
                        SUPPLEMENTAL_KEY, WAIT_FOR_MINUTES, TimeUnit.MINUTES))
                .thenReturn(Optional.of(callbackData));

        // when
        final Throwable thrown =
                catchThrowable(() -> ukOpenBankingPaymentAuthenticator.authenticate(CONSENT_ID));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasNoCause()
                .hasMessageStartingWith("callbackData did not contain code.");

        verifyThirdPartyAppWasOpened();
    }

    private void verifyThirdPartyAppWasOpened() {
        final ArgumentCaptor<ThirdPartyAppAuthenticationPayload> payloadArgumentCaptor =
                ArgumentCaptor.forClass(ThirdPartyAppAuthenticationPayload.class);
        verify(supplementalInformationHelperMock)
                .openThirdPartyApp(payloadArgumentCaptor.capture());

        final ThirdPartyAppAuthenticationPayload actualPayload = payloadArgumentCaptor.getValue();
        assertThat(actualPayload.getDesktop().getUrl()).isEqualTo(AUTHORIZE_URL);
    }

    private static UkOpenBankingPaymentApiClient createApiClientMock(ClientInfo clientInfoMock) {
        final UkOpenBankingPaymentApiClient apiClientMock =
                mock(UkOpenBankingPaymentApiClient.class);

        when(apiClientMock.buildAuthorizeUrl(STATE, CALLBACK_URL, clientInfoMock, CONSENT_ID))
                .thenReturn(new URL(AUTHORIZE_URL));

        return apiClientMock;
    }
}
