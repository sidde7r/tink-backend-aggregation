package se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank.client.ConsorsbankAuthApiClient;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class ConsorsbankAuthenticatorTest {

    private static final String TEST_REDIRECT_URL = "http://test.redirect.example.com";
    private static final String TEST_SCA_REDIRECT = "http://test.sca.redirect.example.com";
    private static final String TEST_CONSENT_ID = "test_consent_id";
    private static final String TEST_STATE = "test_state";
    private static final String TEST_SUPP_KEY = "test_supp_key";

    private ConsorsbankAuthApiClient mockApiClient;
    private ConsorsbankStorage mockStorage;
    private SupplementalInformationController mockSupplementalInformationController;
    private StrongAuthenticationState mockStrongAuthenticationState;
    private Credentials mockCredentials;

    private ConsorsbankAuthenticator authenticator;

    @Before
    public void setup() {
        mockApiClient = mock(ConsorsbankAuthApiClient.class);
        mockStorage = mock(ConsorsbankStorage.class);
        mockSupplementalInformationController = mock(SupplementalInformationController.class);
        mockStrongAuthenticationState = mock(StrongAuthenticationState.class);
        mockCredentials = mock(Credentials.class);

        when(mockStrongAuthenticationState.getState()).thenReturn(TEST_STATE);
        when(mockStrongAuthenticationState.getSupplementalKey()).thenReturn(TEST_SUPP_KEY);

        authenticator =
                new ConsorsbankAuthenticator(
                        mockApiClient,
                        mockStorage,
                        mockSupplementalInformationController,
                        mockStrongAuthenticationState,
                        mockCredentials,
                        new ConstantLocalDateTimeSource(),
                        TEST_REDIRECT_URL);
    }

    @Test
    public void shouldThrowSessionExpiredWhenNoConsentFoundDuringAutoAuthentication() {
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
    public void shouldCreateConsentRequestAndReactToResponseAsExpected() {
        // given
        when(mockApiClient.createConsent(
                        TestDataReader.readFromFile(
                                TestDataReader.CONSENT_REQUEST, ConsentRequest.class),
                        new URL("http://test.redirect.example.com?state=test_state"),
                        new URL("http://test.redirect.example.com?state=test_state&nok=true")))
                .thenReturn(
                        TestDataReader.readFromFile(
                                TestDataReader.CONSENT_CREATED, ConsentResponse.class));

        // when
        ThirdPartyAppAuthenticationPayload appPayload = authenticator.getAppPayload();

        // then
        verify(mockStorage).saveConsentId(TEST_CONSENT_ID);
        assertThat(appPayload.getDesktop().getUrl()).isEqualTo(TEST_SCA_REDIRECT);
    }

    @Test
    public void shouldResultInTimedOutIfNoSupplementalInfoDurignCollect() {
        // given
        when(mockSupplementalInformationController.waitForSupplementalInformation(
                        TEST_SUPP_KEY, 9L, TimeUnit.MINUTES))
                .thenReturn(Optional.empty());

        // when
        ThirdPartyAppResponse<String> collectResult = authenticator.collect("this doesn't matter");

        // then
        assertThat(collectResult.getStatus()).isEqualTo(ThirdPartyAppStatus.TIMED_OUT);
    }

    @Test
    public void shouldResultInCancelledWhenSupplementalDataContainsNotOk() {
        // given
        Map<String, String> callbackData = new HashMap<>();
        callbackData.put("nok", "true");

        when(mockSupplementalInformationController.waitForSupplementalInformation(
                        TEST_SUPP_KEY, 9L, TimeUnit.MINUTES))
                .thenReturn(Optional.of(callbackData));

        // when
        ThirdPartyAppResponse<String> collectResult = authenticator.collect("this doesn't matter");

        // then
        assertThat(collectResult.getStatus()).isEqualTo(ThirdPartyAppStatus.CANCELLED);
    }

    @Test
    public void shouldResultInAuthErrorWhenUnderlyingConsentNotValidAfterCallback() {
        // given
        Map<String, String> callbackData = new HashMap<>();
        when(mockSupplementalInformationController.waitForSupplementalInformation(
                        TEST_SUPP_KEY, 9L, TimeUnit.MINUTES))
                .thenReturn(Optional.of(callbackData));
        when(mockStorage.getConsentId()).thenReturn(TEST_CONSENT_ID);
        when(mockApiClient.fetchConsentDetails(TEST_CONSENT_ID))
                .thenReturn(
                        TestDataReader.readFromFile(
                                TestDataReader.CONSENT_DETAILS_NOT_OK,
                                ConsentDetailsResponse.class));
        // when
        ThirdPartyAppResponse<String> collectResult = authenticator.collect("this doesn't matter");

        // then
        assertThat(collectResult.getStatus()).isEqualTo(ThirdPartyAppStatus.AUTHENTICATION_ERROR);
    }

    @Test
    public void shouldResultInDoneWhenUnderlyingConsentValidAfterCallback() {
        // given
        Map<String, String> callbackData = new HashMap<>();
        when(mockSupplementalInformationController.waitForSupplementalInformation(
                        TEST_SUPP_KEY, 9L, TimeUnit.MINUTES))
                .thenReturn(Optional.of(callbackData));
        when(mockStorage.getConsentId()).thenReturn(TEST_CONSENT_ID);

        ConsentDetailsResponse consentDetailsResponse =
                TestDataReader.readFromFile(
                        TestDataReader.CONSENT_DETAILS_OK, ConsentDetailsResponse.class);
        when(mockApiClient.fetchConsentDetails(TEST_CONSENT_ID)).thenReturn(consentDetailsResponse);
        // when
        ThirdPartyAppResponse<String> collectResult = authenticator.collect("this doesn't matter");

        // then
        verify(mockStorage).saveConsentAccess(eq(consentDetailsResponse.getAccess()));
        assertThat(collectResult.getStatus()).isEqualTo(ThirdPartyAppStatus.DONE);
    }
}
