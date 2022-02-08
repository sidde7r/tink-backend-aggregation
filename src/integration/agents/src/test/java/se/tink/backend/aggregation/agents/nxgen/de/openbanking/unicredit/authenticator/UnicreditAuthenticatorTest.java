package se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.UnicreditApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.authenticator.detail.UnicreditEmbeddedFieldBuilder;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.authenticator.rpc.UnicreditConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditStorage;
import se.tink.backend.aggregation.agents.utils.berlingroup.common.LinksEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthorizationResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;

public class UnicreditAuthenticatorTest {

    private static final String STATE = "state";
    private static final String DUMMY_URL = "dummyUrl";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String OTP = "123456";
    private static final String FIELD_NAME = "any";
    private static final String CONSENT_ID = "id";

    private final UnicreditApiClient apiClient = mock(UnicreditApiClient.class);
    private final UnicreditStorage storage = mock(UnicreditStorage.class);
    private final Credentials credentials = mock(Credentials.class);
    private final StrongAuthenticationState strongAuthenticationState =
            mock(StrongAuthenticationState.class);
    private final SupplementalInformationController supplementalInformationController =
            mock(SupplementalInformationController.class, RETURNS_DEEP_STUBS);
    private final UnicreditEmbeddedFieldBuilder embeddedFieldBuilder =
            mock(UnicreditEmbeddedFieldBuilder.class);

    private final UnicreditAuthenticator authenticator =
            new UnicreditAuthenticator(
                    apiClient,
                    storage,
                    credentials,
                    strongAuthenticationState,
                    supplementalInformationController,
                    embeddedFieldBuilder);

    @Test
    public void autoAuthenticateShouldSetSessionExpiryDate() {
        // given
        ConsentDetailsResponse mockResponse = mock(ConsentDetailsResponse.class);
        LocalDate now = LocalDate.now();
        given(storage.getConsentId()).willReturn(Optional.of(CONSENT_ID));
        given(apiClient.getConsentDetails(CONSENT_ID)).willReturn(mockResponse);
        given(mockResponse.isValid()).willReturn(true);
        given(mockResponse.getValidUntil()).willReturn(now);
        // when
        // then
        assertThatCode(authenticator::autoAuthenticate).doesNotThrowAnyException();
        verify(credentials).setSessionExpiryDate(now);
    }

    @Test
    public void autoAuthenticateShouldThrowExceptionOnMissingConsentId() {
        // given
        given(storage.getConsentId()).willReturn(Optional.empty());
        // when
        Throwable throwable = catchThrowable(authenticator::autoAuthenticate);
        // then
        assertThat(throwable).isInstanceOf(SessionException.class);
    }

    @Test
    public void autoAuthenticateShouldThrowExceptionOnInvalidConsent() {
        // given
        ConsentDetailsResponse mockResponse = mock(ConsentDetailsResponse.class);
        given(storage.getConsentId()).willReturn(Optional.of(CONSENT_ID));
        given(apiClient.getConsentDetails(CONSENT_ID)).willReturn(mockResponse);
        given(mockResponse.isValid()).willReturn(false);
        // when
        Throwable throwable = catchThrowable(authenticator::autoAuthenticate);
        // then
        assertThat(throwable).isInstanceOf(SessionException.class);
    }

    @Test
    public void authenticateShouldCallApiAndSaveConsent() {
        // given
        UnicreditConsentResponse mockUnicreditConsentResponse = mockUnicreditConsentResponse();
        AuthorizationResponse mockAuthResponse = mockAuthResponse();
        Field mockField = mockField();
        mockCredentials();

        ConsentDetailsResponse mockResponse = mock(ConsentDetailsResponse.class);
        LocalDate now = LocalDate.now();
        given(apiClient.getConsentDetails(CONSENT_ID)).willReturn(mockResponse);
        given(mockResponse.isValid()).willReturn(true);
        given(mockResponse.getValidUntil()).willReturn(now);

        given(strongAuthenticationState.getState()).willReturn(STATE);
        given(embeddedFieldBuilder.getOtpFields(any(), any()))
                .willReturn(Collections.singletonList(mockField));
        given(
                        supplementalInformationController
                                .askSupplementalInformationSync(mockField)
                                .get(FIELD_NAME))
                .willReturn(OTP);

        given(apiClient.createConsent(any())).willReturn(mockUnicreditConsentResponse);
        given(apiClient.initializeAuthorization(any(), any(), any())).willReturn(mockAuthResponse);
        given(apiClient.authorizeWithPassword(any(), any(), any())).willReturn(mockAuthResponse);
        given(apiClient.getConsentDetails(any())).willReturn(mockResponse);
        // when
        // then
        assertThatCode(() -> authenticator.authenticate(credentials)).doesNotThrowAnyException();
        verify(credentials).setSessionExpiryDate(now);
        verify(storage).saveConsentId(CONSENT_ID);
        verify(apiClient).initializeAuthorization(DUMMY_URL, STATE, USERNAME);
        verify(apiClient).authorizeWithPassword(DUMMY_URL, USERNAME, PASSWORD);
        verify(apiClient).finalizeAuthorization(DUMMY_URL, OTP);
    }

    @Test
    public void authenticateShouldThrowExceptionOnMissingOtp() {
        // given
        UnicreditConsentResponse mockUnicreditConsentResponse = mockUnicreditConsentResponse();
        AuthorizationResponse mockAuthResponse = mockAuthResponse();
        mockField();
        mockCredentials();

        given(strongAuthenticationState.getState()).willReturn(STATE);

        given(apiClient.createConsent(any())).willReturn(mockUnicreditConsentResponse);
        given(apiClient.initializeAuthorization(any(), any(), any())).willReturn(mockAuthResponse);
        given(apiClient.authorizeWithPassword(any(), any(), any())).willReturn(mockAuthResponse);
        // when
        Throwable throwable = catchThrowable(() -> authenticator.authenticate(credentials));
        // then
        assertThat(throwable).isInstanceOf(SupplementalInfoException.class);
    }

    @Test
    public void authenticatePaymentShouldCallApi() {
        // given
        LinksEntity mockLinksEntity = mock(LinksEntity.class);
        given(mockLinksEntity.getStartAuthorisation()).willReturn(DUMMY_URL);
        AuthorizationResponse mockAuthResponse = mockAuthResponse();
        Field mockField = mockField();
        mockCredentials();

        given(strongAuthenticationState.getState()).willReturn(STATE);
        given(embeddedFieldBuilder.getOtpFields(any(), any()))
                .willReturn(Collections.singletonList(mockField));
        given(
                        supplementalInformationController
                                .askSupplementalInformationSync(mockField)
                                .get(FIELD_NAME))
                .willReturn(OTP);

        given(apiClient.initializeAuthorization(any(), any(), any())).willReturn(mockAuthResponse);
        given(apiClient.authorizeWithPassword(any(), any(), any())).willReturn(mockAuthResponse);
        // when
        // then
        assertThatCode(() -> authenticator.authenticatePayment(mockLinksEntity))
                .doesNotThrowAnyException();
        verify(apiClient).initializeAuthorization(DUMMY_URL, STATE, USERNAME);
        verify(apiClient).authorizeWithPassword(DUMMY_URL, USERNAME, PASSWORD);
        verify(apiClient).finalizeAuthorization(DUMMY_URL, OTP);
    }

    private void mockCredentials() {
        given(credentials.getField(Key.USERNAME)).willReturn(USERNAME);
        given(credentials.getField(Key.PASSWORD)).willReturn(PASSWORD);
    }

    private AuthorizationResponse mockAuthResponse() {
        AuthorizationResponse mock = mock(AuthorizationResponse.class, RETURNS_DEEP_STUBS);
        given(mock.getLinks().getUpdatePsuAuthentication()).willReturn(DUMMY_URL);
        given(mock.getLinks().getAuthoriseTransaction()).willReturn(DUMMY_URL);
        return mock;
    }

    private Field mockField() {
        Field mock = mock(Field.class);
        given(mock.isImmutable()).willReturn(false);
        given(mock.getName()).willReturn(FIELD_NAME);
        return mock;
    }

    private UnicreditConsentResponse mockUnicreditConsentResponse() {
        UnicreditConsentResponse mock = mock(UnicreditConsentResponse.class, RETURNS_DEEP_STUBS);
        given(mock.getConsentId()).willReturn(CONSENT_ID);
        given(mock.getLinks().getStartAuthorisation()).willReturn(DUMMY_URL);
        return mock;
    }
}
