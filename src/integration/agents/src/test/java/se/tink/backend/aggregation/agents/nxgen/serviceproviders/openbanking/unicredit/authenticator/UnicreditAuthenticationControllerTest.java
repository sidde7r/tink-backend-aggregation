package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator;

import static io.vavr.Predicates.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.StorageKeys.CONSENT_ID;
import static se.tink.libraries.date.ThreadSafeDateFormat.FORMATTER_DAILY;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RunWith(JUnitParamsRunner.class)
public class UnicreditAuthenticationControllerTest {

    private static final StrongAuthenticationState STRONG_AUTHENTICATION_STATE =
            new StrongAuthenticationState("STRONG_AUTHENTICATION_STATE");

    /*
     * Mock
     */
    private UnicreditBaseApiClient apiClientMock;
    private SupplementalInformationHelper supplementalInformationHelperMock;

    private InOrder mocksInOrder;

    /*
     * Real
     */
    private PersistentStorage persistentStorage;
    private UnicreditPersistentStorage unicreditStorage;
    private Credentials credentials;
    private UnicreditAuthenticationController authenticatorController;
    private ThirdPartyAppAuthenticationController<String> thirdPartyAppAuthenticationController;

    @Before
    public void setUp() {
        apiClientMock = mock(UnicreditBaseApiClient.class);
        supplementalInformationHelperMock = mock(SupplementalInformationHelper.class);
        mocksInOrder = inOrder(apiClientMock, supplementalInformationHelperMock);

        mockDontOpenAnyThirdPartyUrl();
        mockUserImmediatelyComesBackFromThirdPartyUrl();

        persistentStorage = new PersistentStorage();
        unicreditStorage = new UnicreditPersistentStorage(persistentStorage);
        credentials = new Credentials();

        UnicreditAuthenticator authenticator =
                new UnicreditAuthenticator(unicreditStorage, apiClientMock, credentials);
        authenticatorController =
                new UnicreditAuthenticationController(
                        supplementalInformationHelperMock,
                        authenticator,
                        STRONG_AUTHENTICATION_STATE);
        thirdPartyAppAuthenticationController =
                new ThirdPartyAppAuthenticationController<>(
                        authenticatorController, supplementalInformationHelperMock);
    }

    @Test
    @SneakyThrows
    @Parameters({"2010-04-12", "2030-05-01"})
    public void should_save_consent_id_and_set_session_expiry_date_on_successful_manual_auth(
            String consentValidUntil) {
        // given
        ConsentResponse consentResponse =
                mockApiReturnsNewConsentWithIdAndScaRedirect(
                        "SAMPLE_CONSENT_ID", "https://sca.redirect.com");

        mockConsentHasValidStatus();
        mockConsentIsValidUntil(consentValidUntil);

        // when
        thirdPartyAppAuthenticationController.authenticate(credentials);

        // then
        assertThat(getConsentIdFromPersistentStorage()).hasValue("SAMPLE_CONSENT_ID");
        assertThat(credentials.getSessionExpiryDate())
                .isEqualTo(FORMATTER_DAILY.parse(consentValidUntil));

        verifyConsentCreation(consentResponse);
        verifyRedirectingAndWaitingForUser(consentResponse);
        verifyGettingConsentStatus();
        verifyGettingConsentValidUntilDate();
        verifyNoMoreMockInteractions();
    }

    @Test
    @Parameters({"2010/04/12", "01-05-2030"})
    public void
            should_throw_third_party_authentication_error_when_consent_valid_until_date_has_invalid_format_in_manual_auth(
                    String consentValidUntil) {
        // given
        ConsentResponse consentResponse =
                mockApiReturnsNewConsentWithIdAndScaRedirect(
                        "SAMPLE_CONSENT_ID_0", "https://sca.redirect0.com");

        mockConsentHasValidStatus();
        mockConsentIsValidUntil(consentValidUntil);

        // when
        Throwable throwable =
                catchThrowable(
                        () -> thirdPartyAppAuthenticationController.authenticate(credentials));

        // then
        assertExceptionIsEqualToAgentExceptionFromUsersPerspective(
                throwable, ThirdPartyAppError.AUTHENTICATION_ERROR.exception());

        assertThat(getConsentIdFromPersistentStorage()).hasValue("SAMPLE_CONSENT_ID_0");

        verifyConsentCreation(consentResponse);
        verifyRedirectingAndWaitingForUser(consentResponse);
        verifyGettingConsentStatus();
        verifyGettingConsentValidUntilDate();
        verifyNoMoreMockInteractions();
    }

    @Test
    public void
            should_throw_third_party_authentication_error_and_clear_consent_when_consent_is_invalid_in_manual_auth() {
        // given
        ConsentResponse consentResponse =
                mockApiReturnsNewConsentWithIdAndScaRedirect(
                        "INVALID_MANUAL_AUTH_CONSENT_ID", "https://sca.redirect321.com");

        mockConsentHasInvalidStatus();

        // when
        Throwable throwable =
                catchThrowable(
                        () -> thirdPartyAppAuthenticationController.authenticate(credentials));

        // then
        assertExceptionIsEqualToAgentExceptionFromUsersPerspective(
                throwable, ThirdPartyAppError.AUTHENTICATION_ERROR.exception());

        assertThat(getConsentIdFromPersistentStorage()).isEmpty();

        verifyConsentCreation(consentResponse);
        verifyRedirectingAndWaitingForUser(consentResponse);
        verifyGettingConsentStatus();
        verifyNoMoreMockInteractions();
    }

    @Test
    @Parameters(method = "consentStatusHreKnownInvalidConsentMessages")
    public void
            should_throw_third_party_authentication_error_and_clear_consent_when_consent_is_invalid_by_hre_in_manual_auth(
                    String hreMessage) {
        // given
        ConsentResponse consentResponse =
                mockApiReturnsNewConsentWithIdAndScaRedirect(
                        "INVALID_MANUAL_AUTH_CONSENT_ID_12345", "https://sca.redirect12345.com");

        HttpResponseException hre = responseExceptionWithMessage(hreMessage);
        mockGettingConsentStatusThrowsException(hre);

        // when
        Throwable throwable =
                catchThrowable(
                        () -> thirdPartyAppAuthenticationController.authenticate(credentials));

        // then
        assertExceptionIsEqualToAgentExceptionFromUsersPerspective(
                throwable, ThirdPartyAppError.AUTHENTICATION_ERROR.exception());

        assertThat(getConsentIdFromPersistentStorage()).isEmpty();

        verifyConsentCreation(consentResponse);
        verifyRedirectingAndWaitingForUser(consentResponse);
        verifyGettingConsentStatus();
        verifyNoMoreMockInteractions();
    }

    @Test
    @Parameters(method = "consentStatusHreUnknownMessages")
    public void should_rethrow_consent_status_fetching_hre_that_has_unknown_message_in_manual_auth(
            String hreMessage) {
        // given
        ConsentResponse consentResponse =
                mockApiReturnsNewConsentWithIdAndScaRedirect(
                        "INVALID_MANUAL_AUTH_CONSENT_ID_777", "https://sca.redirect777.com");

        HttpResponseException hre = responseExceptionWithMessage(hreMessage);
        mockGettingConsentStatusThrowsException(hre);

        // when
        Throwable throwable =
                catchThrowable(
                        () -> thirdPartyAppAuthenticationController.authenticate(credentials));

        // then
        assertThat(throwable).isEqualTo(hre);
        assertThat(getConsentIdFromPersistentStorage())
                .hasValue("INVALID_MANUAL_AUTH_CONSENT_ID_777");

        verifyConsentCreation(consentResponse);
        verifyRedirectingAndWaitingForUser(consentResponse);
        verifyGettingConsentStatus();
        verifyNoMoreMockInteractions();
    }

    @Test
    public void
            should_throw_session_expired_exception_when_there_is_no_consent_saved_in_auto_auth() {
        // when
        Throwable throwable = catchThrowable(() -> authenticatorController.autoAuthenticate());

        // then
        assertExceptionIsEqualToAgentExceptionFromUsersPerspective(
                throwable, SessionError.SESSION_EXPIRED.exception());
    }

    @Test
    public void
            should_clear_consent_and_throw_session_expired_exception_when_consent_saved_is_invalid_in_auto_auth() {
        // given
        unicreditStorage.saveConsentId("INVALID_CONSENT_ID1");

        mockConsentHasInvalidStatus();

        // when
        Throwable throwable = catchThrowable(() -> authenticatorController.autoAuthenticate());

        // then
        assertExceptionIsEqualToAgentExceptionFromUsersPerspective(
                throwable, SessionError.SESSION_EXPIRED.exception());
        assertThat(getConsentIdFromPersistentStorage()).isEmpty();
    }

    @Test
    @Parameters(method = "consentStatusHreKnownInvalidConsentMessages")
    public void
            should_throw_third_party_authentication_error_and_clear_consent_when_consent_is_invalid_by_hre_in_auto_auth(
                    String hreMessage) {
        // given
        unicreditStorage.saveConsentId("INVALID_CONSENT_ID2");

        HttpResponseException hre = responseExceptionWithMessage(hreMessage);
        mockGettingConsentStatusThrowsException(hre);

        // when
        Throwable throwable = catchThrowable(() -> authenticatorController.autoAuthenticate());

        // then
        assertExceptionIsEqualToAgentExceptionFromUsersPerspective(
                throwable, SessionError.SESSION_EXPIRED.exception());
        assertThat(getConsentIdFromPersistentStorage()).isEmpty();
    }

    @Test
    @Parameters(method = "consentStatusHreUnknownMessages")
    public void should_rethrow_consent_status_fetching_hre_that_has_unknown_message_in_auto_auth(
            String hreMessage) {
        // given
        unicreditStorage.saveConsentId("INVALID_CONSENT_ID3");

        HttpResponseException hre = responseExceptionWithMessage(hreMessage);
        mockGettingConsentStatusThrowsException(hre);

        // when
        Throwable throwable = catchThrowable(() -> authenticatorController.autoAuthenticate());

        // then
        assertThat(throwable).isEqualTo(hre);
        assertThat(getConsentIdFromPersistentStorage()).hasValue("INVALID_CONSENT_ID3");
    }

    @Test
    @SneakyThrows
    @Parameters({"2010-11-11", "2040-05-14"})
    public void should_set_session_expiry_date_on_successful_auto_auth(String consentValidUntil) {
        // given
        unicreditStorage.saveConsentId("SAMPLE_CONSENT_ID_123");

        mockConsentHasValidStatus();
        mockConsentIsValidUntil(consentValidUntil);

        // when
        authenticatorController.autoAuthenticate();

        // then
        assertThat(unicreditStorage.getConsentId()).hasValue("SAMPLE_CONSENT_ID_123");
        assertThat(credentials.getSessionExpiryDate())
                .isEqualTo(FORMATTER_DAILY.parse(consentValidUntil));

        verifyGettingConsentStatus();
        verifyGettingConsentValidUntilDate();
        verifyNoMoreMockInteractions();
    }

    @Test
    @Parameters({"2010/04/12", "01-05-2030"})
    public void
            should_throw_third_party_authentication_error_on_incorrect_consent_valid_until_date_format_in_auto_auth(
                    String consentValidUntil) {
        // given
        unicreditStorage.saveConsentId("SAMPLE_CONSENT_ID_1234");

        mockConsentHasValidStatus();
        mockConsentIsValidUntil(consentValidUntil);

        // when
        Throwable throwable = catchThrowable(() -> authenticatorController.autoAuthenticate());

        // then
        assertExceptionIsEqualToAgentExceptionFromUsersPerspective(
                throwable, ThirdPartyAppError.AUTHENTICATION_ERROR.exception());

        assertThat(getConsentIdFromPersistentStorage()).hasValue("SAMPLE_CONSENT_ID_1234");

        verifyGettingConsentStatus();
        verifyGettingConsentValidUntilDate();
        verifyNoMoreMockInteractions();
    }

    private void verifyConsentCreation(ConsentResponse consentResponse) {
        mocksInOrder.verify(apiClientMock).createConsent(STRONG_AUTHENTICATION_STATE.getState());
        mocksInOrder.verify(apiClientMock).getScaRedirectUrlFromConsentResponse(consentResponse);
    }

    private void verifyRedirectingAndWaitingForUser(ConsentResponse consentResponse) {
        mocksInOrder
                .verify(supplementalInformationHelperMock)
                .openThirdPartyApp(
                        ThirdPartyAppAuthenticationPayload.of(
                                URL.of(consentResponse.getScaRedirect())));
        mocksInOrder
                .verify(supplementalInformationHelperMock)
                .waitForSupplementalInformation(
                        STRONG_AUTHENTICATION_STATE.getSupplementalKey(), 9L, TimeUnit.MINUTES);
    }

    @SuppressWarnings("unused")
    private static Object[] consentStatusHreKnownInvalidConsentMessages() {
        return getMapDeterminingIfHreMessageIsInvalidConsentMessage().entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .toArray(Object[]::new);
    }

    @SuppressWarnings("unused")
    private static Object[] consentStatusHreUnknownMessages() {
        return getMapDeterminingIfHreMessageIsInvalidConsentMessage().entrySet().stream()
                .filter(not(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .toArray(Object[]::new);
    }

    private static Map<String, Boolean> getMapDeterminingIfHreMessageIsInvalidConsentMessage() {
        Map<String, Boolean> invalidConsentMap =
                ImmutableMap.of(
                        "CONSENT_INVALID", true,
                        " CONSENT_INVALID ", true,
                        "1CONSENT_INVALID1", true,
                        "consent_invalid", false,
                        "CONSENT_INVALI D", false);
        Map<String, Boolean> consentExpiredMap =
                ImmutableMap.of(
                        "CONSENT_EXPIRED", true,
                        " CONSENT_EXPIRED ", true,
                        "1CONSENT_EXPIRED1", true,
                        "consent_expired", false,
                        "CONSENT_EXPIRE D", false);
        Map<String, Boolean> consentUnknownMap =
                ImmutableMap.of(
                        "CONSENT_UNKNOWN", true,
                        " CONSENT_UNKNOWN ", true,
                        "1CONSENT_UNKNOWN1", true,
                        "consent_unknown", false,
                        "CONSENT_UNKNOW N", false);
        return Stream.of(invalidConsentMap, consentExpiredMap, consentUnknownMap)
                .map(Map::entrySet)
                .flatMap(Set::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Optional<String> getConsentIdFromPersistentStorage() {
        return persistentStorage.get(CONSENT_ID, String.class);
    }

    private void verifyGettingConsentStatus() {
        mocksInOrder.verify(apiClientMock).getConsentStatus();
    }

    private void verifyGettingConsentValidUntilDate() {
        mocksInOrder.verify(apiClientMock).getConsentDetails();
    }

    private void verifyNoMoreMockInteractions() {
        mocksInOrder.verifyNoMoreInteractions();
    }

    private void assertExceptionIsEqualToAgentExceptionFromUsersPerspective(
            Throwable throwable, AgentException expectedException) {
        assertThat(throwable).isInstanceOf(expectedException.getClass());
        assertThat(((AgentException) throwable).getError()).isEqualTo(expectedException.getError());
        assertThat(((AgentException) throwable).getUserMessage())
                .isEqualTo(expectedException.getUserMessage());
    }

    private HttpResponseException responseExceptionWithMessage(String message) {
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getBody(String.class)).thenReturn(message);

        HttpResponseException exception = mock(HttpResponseException.class);
        when(exception.getResponse()).thenReturn(httpResponse);

        return exception;
    }

    private ConsentResponse mockApiReturnsNewConsentWithIdAndScaRedirect(
            String consentId, String scaRedirect) {
        ConsentResponse consentResponse = mock(ConsentResponse.class);
        when(consentResponse.getConsentId()).thenReturn(consentId);
        when(consentResponse.getScaRedirect()).thenReturn(scaRedirect);

        when(apiClientMock.createConsent(any())).thenReturn(consentResponse);
        when(apiClientMock.getScaRedirectUrlFromConsentResponse(any()))
                .thenReturn(URL.of(scaRedirect));

        return consentResponse;
    }

    private void mockUserImmediatelyComesBackFromThirdPartyUrl() {
        when(supplementalInformationHelperMock.waitForSupplementalInformation(
                        eq(STRONG_AUTHENTICATION_STATE.getSupplementalKey()), anyLong(), any()))
                .thenReturn(Optional.of(Collections.emptyMap()));
    }

    private void mockDontOpenAnyThirdPartyUrl() {
        doNothing().when(supplementalInformationHelperMock).openThirdPartyApp(any());
    }

    private void mockConsentHasValidStatus() {
        ConsentStatusResponse consentStatusResponse = mock(ConsentStatusResponse.class);
        when(consentStatusResponse.isValidConsent()).thenReturn(true);

        when(apiClientMock.getConsentStatus()).thenReturn(consentStatusResponse);
    }

    private void mockConsentHasInvalidStatus() {
        ConsentStatusResponse consentStatusResponse = mock(ConsentStatusResponse.class);
        when(consentStatusResponse.isValidConsent()).thenReturn(false);

        when(apiClientMock.getConsentStatus()).thenReturn(consentStatusResponse);
    }

    private void mockGettingConsentStatusThrowsException(HttpResponseException hre) {
        when(apiClientMock.getConsentStatus()).thenThrow(hre);
    }

    private void mockConsentIsValidUntil(String validUntil) {
        ConsentDetailsResponse consentDetailsResponse = mock(ConsentDetailsResponse.class);
        when(consentDetailsResponse.getValidUntil()).thenReturn(validUntil);

        when(apiClientMock.getConsentDetails()).thenReturn(consentDetailsResponse);
    }
}
