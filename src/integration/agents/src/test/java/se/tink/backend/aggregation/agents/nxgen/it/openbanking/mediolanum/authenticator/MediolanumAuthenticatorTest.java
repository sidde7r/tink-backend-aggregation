package se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.MediolanumApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.MediolanumStorage;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.TestDataReader;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class MediolanumAuthenticatorTest {

    private static final String TEST_STATE = "test_state";
    private static final String TEST_REFERENCE = "test_reference";
    private static final String TEST_CONSENT_ID = "test_consent_id";

    private OAuth2AuthenticationController mockOAuth2AuthenticationController;
    private MediolanumApiClient mockApiClient;
    private SupplementalInformationHelper mockSupplementalInformationHelper;
    private StrongAuthenticationState strongAuthenticationState;
    private MediolanumStorage storage;
    private Credentials credentials;

    private MediolanumAuthenticator authenticator;

    @Before
    public void setup() {
        mockOAuth2AuthenticationController = mock(OAuth2AuthenticationController.class);
        mockApiClient = mock(MediolanumApiClient.class);
        mockSupplementalInformationHelper = mock(SupplementalInformationHelper.class);

        storage = new MediolanumStorage(new PersistentStorage());
        credentials = new Credentials();
        strongAuthenticationState = new StrongAuthenticationState(TEST_STATE);

        authenticator =
                new MediolanumAuthenticator(
                        mockOAuth2AuthenticationController,
                        mockApiClient,
                        storage,
                        credentials,
                        strongAuthenticationState,
                        mockSupplementalInformationHelper);
    }

    @Test
    public void collectShouldCreateAndAuthorizeConsent() {
        // given
        when(mockOAuth2AuthenticationController.collect(TEST_REFERENCE))
                .thenReturn(ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE));
        when(mockSupplementalInformationHelper.waitForSupplementalInformation(
                        strongAuthenticationState.getSupplementalKey(), 9, TimeUnit.MINUTES))
                .thenReturn(Optional.of(Collections.emptyMap()));

        when(mockApiClient.createConsent(strongAuthenticationState.getState()))
                .thenReturn(
                        TestDataReader.readFromFile(
                                TestDataReader.CONSENT_CREATED, ConsentResponse.class));
        when(mockApiClient.fetchConsentDetails(TEST_CONSENT_ID))
                .thenReturn(
                        TestDataReader.readFromFile(
                                TestDataReader.CONSENT_DETAILS_OK, ConsentDetailsResponse.class));
        // when
        ThirdPartyAppResponse<String> collectResponse = authenticator.collect(TEST_REFERENCE);
        // then
        assertThat(storage.getConsentId()).isEqualTo(TEST_CONSENT_ID);
        assertThat(credentials.getSessionExpiryDate()).hasYear(2020);
        assertThat(credentials.getSessionExpiryDate()).hasMonth(11);
        assertThat(credentials.getSessionExpiryDate()).hasDayOfMonth(11);

        assertThat(collectResponse.getStatus()).isEqualTo(ThirdPartyAppStatus.DONE);
    }

    @Test
    public void collectShouldNotCreateConsentIfOauthPartDidNotFinishSuccessfully() {
        // given
        when(mockOAuth2AuthenticationController.collect(TEST_REFERENCE))
                .thenReturn(
                        ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.AUTHENTICATION_ERROR));
        // when
        ThirdPartyAppResponse<String> collectResponse = authenticator.collect(TEST_REFERENCE);

        // then
        assertThat(collectResponse.getStatus()).isEqualTo(ThirdPartyAppStatus.AUTHENTICATION_ERROR);
        verifyNoMoreInteractions(mockApiClient);
    }

    @Test
    public void shouldEndUpWithTimedOutIfNoCallbackComes() {
        // given
        when(mockOAuth2AuthenticationController.collect(TEST_REFERENCE))
                .thenReturn(ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE));
        when(mockSupplementalInformationHelper.waitForSupplementalInformation(
                        strongAuthenticationState.getSupplementalKey(), 9, TimeUnit.MINUTES))
                .thenReturn(Optional.empty());
        when(mockApiClient.createConsent(strongAuthenticationState.getState()))
                .thenReturn(
                        TestDataReader.readFromFile(
                                TestDataReader.CONSENT_CREATED, ConsentResponse.class));

        // when
        Throwable throwable = catchThrowable(() -> authenticator.collect(TEST_REFERENCE));

        // then
        assertThat(throwable)
                .isInstanceOf(ThirdPartyAppException.class)
                .hasMessage("Cause: ThirdPartyAppError.TIMED_OUT");
        assertThat(storage.getConsentId()).isNull();
    }

    @Test
    public void shouldThrowDefaultErrorWhenNotOkCallbackComes() {
        // given
        Map<String, String> callbackData = new HashMap<>();
        callbackData.put("nok", "true");

        when(mockOAuth2AuthenticationController.collect(TEST_REFERENCE))
                .thenReturn(ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE));
        when(mockSupplementalInformationHelper.waitForSupplementalInformation(
                        strongAuthenticationState.getSupplementalKey(), 9, TimeUnit.MINUTES))
                .thenReturn(Optional.of(callbackData));
        when(mockApiClient.createConsent(strongAuthenticationState.getState()))
                .thenReturn(
                        TestDataReader.readFromFile(
                                TestDataReader.CONSENT_CREATED, ConsentResponse.class));

        // when
        Throwable throwable = catchThrowable(() -> authenticator.collect(TEST_REFERENCE));

        // then
        assertThat(throwable)
                .isInstanceOf(LoginException.class)
                .hasMessage("Callback received on NOK endpoint, aborting.");
        assertThat(storage.getConsentId()).isNull();
    }

    @Test
    public void shouldThrowDefaultErrorWhenConsentNotOkAfterCallback() {
        // given
        when(mockOAuth2AuthenticationController.collect(TEST_REFERENCE))
                .thenReturn(ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE));
        when(mockSupplementalInformationHelper.waitForSupplementalInformation(
                        strongAuthenticationState.getSupplementalKey(), 9, TimeUnit.MINUTES))
                .thenReturn(Optional.of(Collections.emptyMap()));

        when(mockApiClient.createConsent(strongAuthenticationState.getState()))
                .thenReturn(
                        TestDataReader.readFromFile(
                                TestDataReader.CONSENT_CREATED, ConsentResponse.class));
        when(mockApiClient.fetchConsentDetails(TEST_CONSENT_ID))
                .thenReturn(
                        TestDataReader.readFromFile(
                                TestDataReader.CONSENT_DETAILS_NOT_OK,
                                ConsentDetailsResponse.class));
        // when
        Throwable throwable = catchThrowable(() -> authenticator.collect(TEST_REFERENCE));

        // then
        assertThat(throwable)
                .isInstanceOf(LoginException.class)
                .hasMessage("Consent not valid after callback.");
        assertThat(storage.getConsentId()).isNull();
    }
}
