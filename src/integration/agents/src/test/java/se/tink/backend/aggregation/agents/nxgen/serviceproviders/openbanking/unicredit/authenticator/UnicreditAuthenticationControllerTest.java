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

import com.google.common.collect.ImmutableMap;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.UnicreditConsentResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class UnicreditAuthenticationControllerTest {

    private static final StrongAuthenticationState STRONG_AUTHENTICATION_STATE =
            new StrongAuthenticationState("STRONG_AUTHENTICATION_STATE");

    /*
     Test data
    */
    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/unicredit/resources";

    public static final String CONSENT_ID = "SAMPLE_CONSENT_ID";
    public static final LocalDate VALID_UNTIL =
            LocalDate.parse("2020-03-17", DateTimeFormatter.ISO_DATE);
    public static final Date VALID_UNTIL_AS_DATE = localDateToDate(VALID_UNTIL);

    /*
     Mock
    */
    private UnicreditBaseApiClient apiClientMock;
    private SupplementalInformationHelper supplementalInformationHelperMock;

    private InOrder mocksInOrder;

    /*
     Real
    */
    private PersistentStorage persistentStorage;
    private UnicreditStorage unicreditStorage;
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
        unicreditStorage = new UnicreditStorage(persistentStorage);
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
    public void should_save_consent_id_and_set_session_expiry_date_on_successful_manual_auth() {
        // given
        ConsentResponse consentResponse = mockCreateConsentResponse("createConsentResponse.json");
        mockConsentDetailsResponse("validConsentDetails.json");

        // when
        thirdPartyAppAuthenticationController.authenticate(credentials);

        // then
        assertThat(getConsentIdFromPersistentStorage()).hasValue(CONSENT_ID);
        assertThat(credentials.getSessionExpiryDate()).isEqualTo(VALID_UNTIL_AS_DATE);

        verifyConsentCreation(consentResponse);
        verifyRedirectingAndWaitingForUser(consentResponse);
        verifyGettingConsentDetails();
        verifyNoMoreMockInteractions();
    }

    @Test
    @Parameters({"EXPIRED", "REVOKED_BY_PSU", "@!%^#$"})
    public void
            should_throw_third_party_authentication_error_and_clear_consent_when_consent_is_invalid_in_manual_auth(
                    String consentStatus) {
        // given
        ConsentResponse consentResponse = mockCreateConsentResponse("createConsentResponse.json");

        ConsentDetailsResponse consentDetailsResponse =
                consentDetailsResponseFromFile("validConsentDetails.json");
        consentDetailsResponse.setConsentStatus(consentStatus);
        mockConsentDetailsResponse(consentDetailsResponse);

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
        verifyGettingConsentDetails();
        verifyNoMoreMockInteractions();
    }

    @Test
    @Parameters(method = "consentStatusHreKnownInvalidConsentMessages")
    public void
            should_throw_third_party_authentication_error_and_clear_consent_when_consent_is_invalid_by_hre_in_manual_auth(
                    String hreMessage) {
        // given
        ConsentResponse consentResponse = mockCreateConsentResponse("createConsentResponse.json");

        HttpResponseException hre = responseExceptionWithMessage(hreMessage);
        mockGettingConsentDetailsThrowsException(hre);

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
        verifyGettingConsentDetails();
        verifyNoMoreMockInteractions();
    }

    @Test
    @Parameters(method = "consentStatusHreUnknownMessages")
    public void should_rethrow_consent_status_fetching_hre_that_has_unknown_message_in_manual_auth(
            String hreMessage) {
        // given
        ConsentResponse consentResponse = mockCreateConsentResponse("createConsentResponse.json");

        HttpResponseException hre = responseExceptionWithMessage(hreMessage);
        mockGettingConsentDetailsThrowsException(hre);

        // when
        Throwable throwable =
                catchThrowable(
                        () -> thirdPartyAppAuthenticationController.authenticate(credentials));

        // then
        assertThat(throwable).isEqualTo(hre);
        assertThat(getConsentIdFromPersistentStorage()).hasValue(CONSENT_ID);

        verifyConsentCreation(consentResponse);
        verifyRedirectingAndWaitingForUser(consentResponse);
        verifyGettingConsentDetails();
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
    @Parameters({"EXPIRED", "REVOKED_BY_PSU", "@!%^#$"})
    public void
            should_clear_consent_and_throw_session_expired_exception_when_consent_saved_is_invalid_in_auto_auth(
                    String consentStatus) {
        // given
        unicreditStorage.saveConsentId(CONSENT_ID);

        ConsentDetailsResponse consentDetailsResponse =
                consentDetailsResponseFromFile("validConsentDetails.json");
        consentDetailsResponse.setConsentStatus(consentStatus);
        mockConsentDetailsResponse(consentDetailsResponse);

        // when
        Throwable throwable = catchThrowable(() -> authenticatorController.autoAuthenticate());

        // then
        assertExceptionIsEqualToAgentExceptionFromUsersPerspective(
                throwable, SessionError.SESSION_EXPIRED.exception());

        assertThat(getConsentIdFromPersistentStorage()).isEmpty();

        verifyGettingConsentDetails();
    }

    @Test
    @Parameters(method = "consentStatusHreKnownInvalidConsentMessages")
    public void
            should_throw_third_party_authentication_error_and_clear_consent_when_consent_is_invalid_by_hre_in_auto_auth(
                    String hreMessage) {
        // given
        unicreditStorage.saveConsentId(CONSENT_ID);

        HttpResponseException hre = responseExceptionWithMessage(hreMessage);
        mockGettingConsentDetailsThrowsException(hre);

        // when
        Throwable throwable = catchThrowable(() -> authenticatorController.autoAuthenticate());

        // then
        assertExceptionIsEqualToAgentExceptionFromUsersPerspective(
                throwable, SessionError.SESSION_EXPIRED.exception());

        assertThat(getConsentIdFromPersistentStorage()).isEmpty();

        verifyGettingConsentDetails();
    }

    @Test
    @Parameters(method = "consentStatusHreUnknownMessages")
    public void should_rethrow_consent_status_fetching_hre_that_has_unknown_message_in_auto_auth(
            String hreMessage) {
        // given
        unicreditStorage.saveConsentId("INVALID_CONSENT_ID123");

        HttpResponseException hre = responseExceptionWithMessage(hreMessage);
        mockGettingConsentDetailsThrowsException(hre);

        // when
        Throwable throwable = catchThrowable(() -> authenticatorController.autoAuthenticate());

        // then
        assertThat(throwable).isEqualTo(hre);

        assertThat(getConsentIdFromPersistentStorage()).hasValue("INVALID_CONSENT_ID123");

        verifyGettingConsentDetails();
    }

    @Test
    @SneakyThrows
    public void should_set_session_expiry_date_on_successful_auto_auth() {
        // given
        unicreditStorage.saveConsentId(CONSENT_ID);

        mockConsentDetailsResponse("validConsentDetails.json");

        // when
        authenticatorController.autoAuthenticate();

        // then
        assertThat(unicreditStorage.getConsentId()).hasValue(CONSENT_ID);
        assertThat(credentials.getSessionExpiryDate()).isEqualTo(localDateToDate(VALID_UNTIL));

        verifyGettingConsentDetails();
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
        return persistentStorage.get(UnicreditConstants.StorageKeys.CONSENT_ID, String.class);
    }

    private void verifyGettingConsentDetails() {
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

    private void mockUserImmediatelyComesBackFromThirdPartyUrl() {
        when(supplementalInformationHelperMock.waitForSupplementalInformation(
                        eq(STRONG_AUTHENTICATION_STATE.getSupplementalKey()), anyLong(), any()))
                .thenReturn(Optional.of(Collections.emptyMap()));
    }

    private void mockDontOpenAnyThirdPartyUrl() {
        doNothing().when(supplementalInformationHelperMock).openThirdPartyApp(any());
    }

    @SuppressWarnings("SameParameterValue")
    private void mockConsentDetailsResponse(String file) {
        when(apiClientMock.getConsentDetails()).thenReturn(consentDetailsResponseFromFile(file));
    }

    private void mockConsentDetailsResponse(ConsentDetailsResponse consentDetailsResponse) {
        when(apiClientMock.getConsentDetails()).thenReturn(consentDetailsResponse);
    }

    @SuppressWarnings("SameParameterValue")
    private ConsentResponse mockCreateConsentResponse(String file) {
        ConsentResponse consentResponse = consentResponseFromFile(file);
        when(apiClientMock.createConsent(STRONG_AUTHENTICATION_STATE.getState()))
                .thenReturn(consentResponse);
        when(apiClientMock.getScaRedirectUrlFromConsentResponse(consentResponse))
                .thenReturn(URL.of(consentResponse.getScaRedirect()));
        return consentResponse;
    }

    private void mockGettingConsentDetailsThrowsException(HttpResponseException hre) {
        when(apiClientMock.getConsentDetails()).thenThrow(hre);
    }

    @SuppressWarnings("SameParameterValue")
    private static Date localDateToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static UnicreditConsentResponse consentResponseFromFile(String file) {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, file).toFile(), UnicreditConsentResponse.class);
    }

    public static ConsentDetailsResponse consentDetailsResponseFromFile(String file) {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, file).toFile(), ConsentDetailsResponse.class);
    }
}
