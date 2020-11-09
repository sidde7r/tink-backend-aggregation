package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbConstants;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbStorage;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator.rpc.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DnbAuthenticatorTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/openbanking/dnb/resources";
    private static final String TEST_CONSENT_ID = "test_consent_id";
    private static final String TEST_STATE = "test_state";
    private static final String TEST_KEY = "test_key";
    private static final String TEST_SCA_URI =
            "https://www.test.com/segp/appo/logon/psd2Start?blob=ASDF&redirectUrl=https%3A%2F%2Fapi.tink.com%2Fapi%2Fv1%2Fcredentials%2Fthird-party%2Fcallback%3Fstate%3Dtest_state";

    private SupplementalInformationHelper mockSupplemental;
    private StrongAuthenticationState mockState;
    private DnbStorage mockStorage;
    private DnbApiClient mockApiClient;
    private Credentials mockCredentials;

    private DnbAuthenticator authenticator;

    @Before
    public void setup() {
        mockSupplemental = mock(SupplementalInformationHelper.class);
        mockState = mock(StrongAuthenticationState.class);
        mockStorage = mock(DnbStorage.class);
        mockApiClient = mock(DnbApiClient.class);
        mockCredentials = mock(Credentials.class);

        authenticator =
                new DnbAuthenticator(
                        mockSupplemental, mockState, mockStorage, mockApiClient, mockCredentials);
    }

    // auto auth

    @Test
    public void shouldFailAutoAuthWithSessionExpiredWhenConsentMissing() {
        // given
        given(mockStorage.containsConsentId()).willReturn(false);

        // when
        Throwable throwable = catchThrowable(authenticator::autoAuthenticate);

        // then
        assertThat(throwable)
                .isInstanceOf(SessionException.class)
                .hasMessage("Cause: SessionError.SESSION_EXPIRED");
        verify(mockStorage).containsConsentId();
        verifyNoMoreInteractionsOnAllMocks();
    }

    @Test
    public void shouldFailAutoAuthWithSessionExpiredWhenConsentNotValid() {
        // given
        given(mockStorage.containsConsentId()).willReturn(true);
        given(mockStorage.getConsentId()).willReturn(TEST_CONSENT_ID);
        given(mockApiClient.fetchConsentDetails(TEST_CONSENT_ID)).willReturn(getInvalidResponse());

        // when
        Throwable throwable = catchThrowable(authenticator::autoAuthenticate);

        // then
        assertThat(throwable)
                .isInstanceOf(SessionException.class)
                .hasMessage("Cause: SessionError.SESSION_EXPIRED");
        verify(mockStorage).containsConsentId();
        verify(mockStorage).getConsentId();
        verify(mockApiClient).fetchConsentDetails(TEST_CONSENT_ID);
        verifyNoMoreInteractionsOnAllMocks();
    }

    @Test
    public void shouldCompleteAutoAuthWhenConsentStoredAndValid() {
        // given
        given(mockStorage.containsConsentId()).willReturn(true);
        given(mockStorage.getConsentId()).willReturn(TEST_CONSENT_ID);
        given(mockApiClient.fetchConsentDetails(TEST_CONSENT_ID)).willReturn(getValidResponse());

        // when
        authenticator.autoAuthenticate();

        // then
        verify(mockStorage).containsConsentId();
        verify(mockStorage).getConsentId();
        verify(mockApiClient).fetchConsentDetails(TEST_CONSENT_ID);
        verifyNoMoreInteractionsOnAllMocks();
    }

    // init
    @Test
    public void shouldReturnAuthErrorDuringInitIfNoPsuIdInCredentials() {
        // given
        given(mockCredentials.getField(DnbConstants.CredentialsKeys.PSU_ID)).willReturn("");

        // when
        ThirdPartyAppResponse<String> result = authenticator.init();

        // then
        assertThat(result.getStatus()).isEqualTo(ThirdPartyAppStatus.AUTHENTICATION_ERROR);

        verify(mockCredentials).getField(DnbConstants.CredentialsKeys.PSU_ID);
        verifyNoMoreInteractionsOnAllMocks();
    }

    @Test
    public void shouldReturnWaitingDuringInitIfNoPsuIdInCredentials() {
        // given
        given(mockCredentials.getField(DnbConstants.CredentialsKeys.PSU_ID))
                .willReturn("test_psu_id");

        // when
        ThirdPartyAppResponse<String> result = authenticator.init();

        // then
        assertThat(result.getStatus()).isEqualTo(ThirdPartyAppStatus.WAITING);

        verify(mockCredentials).getField(DnbConstants.CredentialsKeys.PSU_ID);
        verifyNoMoreInteractionsOnAllMocks();
    }

    // get app payload

    @Test
    public void shouldStoreConsentIdAndReturnProperUrlWhenSuccessful() {
        // given
        given(mockState.getState()).willReturn(TEST_STATE);
        given(mockApiClient.createConsent(TEST_STATE)).willReturn(getConsentCreationResponse());

        // when
        ThirdPartyAppAuthenticationPayload appPayload = authenticator.getAppPayload();

        // then
        assertThat(appPayload.getDesktop().getUrl()).isEqualTo(TEST_SCA_URI);

        verify(mockState).getState();
        verify(mockApiClient).createConsent(TEST_STATE);
        verify(mockStorage).storeConsentId(TEST_CONSENT_ID);
        verifyNoMoreInteractionsOnAllMocks();
    }

    // collect
    @Test
    public void shouldReturnTimedOutStatusWhenNoSupplementalInfoGathered() {
        // given
        given(mockState.getSupplementalKey()).willReturn(TEST_KEY);
        given(mockSupplemental.waitForSupplementalInformation(TEST_KEY, 9L, TimeUnit.MINUTES))
                .willReturn(Optional.empty());

        // when
        ThirdPartyAppResponse<String> result = authenticator.collect("");

        // then
        assertThat(result.getStatus()).isEqualTo(ThirdPartyAppStatus.TIMED_OUT);

        verify(mockState).getSupplementalKey();
        verify(mockSupplemental).waitForSupplementalInformation(TEST_KEY, 9L, TimeUnit.MINUTES);
        verifyNoMoreInteractionsOnAllMocks();
    }

    @Test
    public void shouldReturnAuthErrorStatusWhenConsentInvalid() {
        // given
        given(mockState.getSupplementalKey()).willReturn(TEST_KEY);
        given(mockSupplemental.waitForSupplementalInformation(TEST_KEY, 9L, TimeUnit.MINUTES))
                .willReturn(Optional.of(new HashMap<>()));
        given(mockStorage.getConsentId()).willReturn(TEST_CONSENT_ID);
        given(mockApiClient.fetchConsentDetails(TEST_CONSENT_ID)).willReturn(getInvalidResponse());

        // when
        ThirdPartyAppResponse<String> result = authenticator.collect("");

        // then
        assertThat(result.getStatus()).isEqualTo(ThirdPartyAppStatus.AUTHENTICATION_ERROR);

        verify(mockState).getSupplementalKey();
        verify(mockSupplemental).waitForSupplementalInformation(TEST_KEY, 9L, TimeUnit.MINUTES);
        verify(mockStorage).getConsentId();
        verify(mockApiClient).fetchConsentDetails(TEST_CONSENT_ID);
        verifyNoMoreInteractionsOnAllMocks();
    }

    @Test
    public void shouldReturnDoneStatusAndSetSessionExpiryWhenConsentValid() {
        // given
        given(mockState.getSupplementalKey()).willReturn(TEST_KEY);
        given(mockSupplemental.waitForSupplementalInformation(TEST_KEY, 9L, TimeUnit.MINUTES))
                .willReturn(Optional.of(new HashMap<>()));
        given(mockStorage.getConsentId()).willReturn(TEST_CONSENT_ID);
        given(mockApiClient.fetchConsentDetails(TEST_CONSENT_ID)).willReturn(getValidResponse());

        // when
        ThirdPartyAppResponse<String> result = authenticator.collect("");

        // then
        assertThat(result.getStatus()).isEqualTo(ThirdPartyAppStatus.DONE);

        verify(mockState).getSupplementalKey();
        verify(mockSupplemental).waitForSupplementalInformation(TEST_KEY, 9L, TimeUnit.MINUTES);
        verify(mockStorage).getConsentId();
        verify(mockApiClient).fetchConsentDetails(TEST_CONSENT_ID);
        verify(mockCredentials).setSessionExpiryDate(LocalDate.of(2021, 1, 20));
        verifyNoMoreInteractionsOnAllMocks();
    }

    private void verifyNoMoreInteractionsOnAllMocks() {
        verifyNoMoreInteractions(
                mockSupplemental, mockState, mockStorage, mockApiClient, mockCredentials);
    }

    private ConsentDetailsResponse getValidResponse() {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, "consentStatusValid.json").toFile(),
                ConsentDetailsResponse.class);
    }

    private ConsentDetailsResponse getInvalidResponse() {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, "consentStatusInvalid.json").toFile(),
                ConsentDetailsResponse.class);
    }

    private ConsentResponse getConsentCreationResponse() {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, "consentCreation.json").toFile(), ConsentResponse.class);
    }
}
