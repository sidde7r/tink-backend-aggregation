package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.SpardaStorage;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.TestDataReader;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.client.SpardaAuthApiClient;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;

public class SpardaAuthenticatorTest {
    private static final String TEST_CONSENT_ID = "test_consent_id";

    private SpardaAuthApiClient mockApiClient;
    private SpardaStorage mockStorage;
    private OAuth2AuthenticationController mockOAuth2AuthenticationController;
    private Credentials mockCredentials;

    private SpardaAuthenticator authenticator;

    @Before
    public void setup() {
        mockApiClient = mock(SpardaAuthApiClient.class);
        mockStorage = mock(SpardaStorage.class);
        mockOAuth2AuthenticationController = mock(OAuth2AuthenticationController.class);
        mockCredentials = mock(Credentials.class);

        authenticator =
                new SpardaAuthenticator(
                        mockApiClient,
                        mockStorage,
                        mockOAuth2AuthenticationController,
                        mockCredentials);
    }

    @Test
    public void shouldThrowSessionExpiredWhenNoConsentFoundDuringAutoAuthentication() {
        // given
        doNothing().when(mockOAuth2AuthenticationController).autoAuthenticate();

        // when
        Throwable throwable = catchThrowable(authenticator::autoAuthenticate);

        // then
        assertThat(throwable)
                .isInstanceOf(SessionException.class)
                .hasMessage("Cause: SessionError.SESSION_EXPIRED");
    }

    @Test
    public void shouldThrowSessionExpiredWhenConsentIsNoLongerValid() {
        // given
        doNothing().when(mockOAuth2AuthenticationController).autoAuthenticate();
        when(mockStorage.getConsentId()).thenReturn(TEST_CONSENT_ID);
        when(mockApiClient.fetchConsentDetails(TEST_CONSENT_ID))
                .thenReturn(
                        TestDataReader.readFromFile(
                                TestDataReader.CONSENT_DETAILS_NOT_OK,
                                ConsentDetailsResponse.class));

        // when
        Throwable throwable = catchThrowable(authenticator::autoAuthenticate);

        // then
        assertThat(throwable)
                .isInstanceOf(SessionException.class)
                .hasMessage("Cause: SessionError.SESSION_EXPIRED");
    }

    @Test
    public void shouldEndWithExceptionWhenOauthAutoRefreshFails() {
        // given
        doThrow(SessionError.SESSION_EXPIRED.exception())
                .when(mockOAuth2AuthenticationController)
                .autoAuthenticate();

        // when
        Throwable throwable = catchThrowable(authenticator::autoAuthenticate);

        // then
        assertThat(throwable)
                .isInstanceOf(SessionException.class)
                .hasMessage("Cause: SessionError.SESSION_EXPIRED");
    }

    @Test
    public void shouldFinishWithDoneAfterOauthPartIsDoneAndConsentIsValid() {
        // given
        when(mockOAuth2AuthenticationController.collect(anyString()))
                .thenReturn(ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE));
        when(mockStorage.getConsentId()).thenReturn(TEST_CONSENT_ID);
        when(mockApiClient.fetchConsentDetails(TEST_CONSENT_ID))
                .thenReturn(
                        TestDataReader.readFromFile(
                                TestDataReader.CONSENT_DETAILS_OK, ConsentDetailsResponse.class));

        // when
        ThirdPartyAppResponse<String> finalResult = authenticator.collect("");

        // then
        verify(mockApiClient).fetchConsentDetails(TEST_CONSENT_ID);
        verify(mockCredentials).setSessionExpiryDate(LocalDate.of(2021, 9, 22));
        assertThat(finalResult.getStatus()).isEqualTo(ThirdPartyAppStatus.DONE);
    }

    @Test
    public void shouldEndWithAuthErrorResultWhenConsentNotValidAfterOauthDone() {
        // given
        when(mockOAuth2AuthenticationController.collect(anyString()))
                .thenReturn(ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE));
        when(mockStorage.getConsentId()).thenReturn(TEST_CONSENT_ID);
        when(mockApiClient.fetchConsentDetails(TEST_CONSENT_ID))
                .thenReturn(
                        TestDataReader.readFromFile(
                                TestDataReader.CONSENT_DETAILS_NOT_OK,
                                ConsentDetailsResponse.class));

        // when
        ThirdPartyAppResponse<String> finalResult = authenticator.collect("");

        // then
        verify(mockApiClient).fetchConsentDetails(TEST_CONSENT_ID);
        assertThat(finalResult.getStatus()).isEqualTo(ThirdPartyAppStatus.AUTHENTICATION_ERROR);
    }
}
