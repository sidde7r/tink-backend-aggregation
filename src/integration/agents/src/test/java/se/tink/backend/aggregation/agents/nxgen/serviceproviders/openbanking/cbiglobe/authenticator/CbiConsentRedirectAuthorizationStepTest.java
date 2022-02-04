package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.TestDataReader;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.CbiConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.CbiConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobeAuthApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class CbiConsentRedirectAuthorizationStepTest {

    private static final CbiConsentResponse CONSENT_RESPONSE =
            TestDataReader.readFromFile(
                    TestDataReader.CREATE_CONSENT_RESP, CbiConsentResponse.class);

    private static final URL TEST_SCA_REDIRECT_URL =
            new URL("https://example.com/redirect%20Me%20Here");
    private static final String TEST_CONSENT_ID = "1234509876";

    private SupplementalInformationController mockSupplementalInformationController;
    private CbiGlobeAuthApiClient mockAuthApiClient;
    private CbiStorage mockStorage;

    private CbiConsentRedirectAuthorizationStep consentRedirectAuthorizationStep;

    @Before
    public void setup() {
        mockSupplementalInformationController = mock(SupplementalInformationController.class);
        mockAuthApiClient = mock(CbiGlobeAuthApiClient.class);
        mockStorage = mock(CbiStorage.class);
        when(mockStorage.getConsentId()).thenReturn(TEST_CONSENT_ID);

        consentRedirectAuthorizationStep =
                new CbiConsentRedirectAuthorizationStep(
                        mockSupplementalInformationController, mockAuthApiClient, mockStorage);
    }

    @Test
    public void shouldThrowTimedOutWhenNoSupplementalInfoReturned() {
        // given
        when(mockSupplementalInformationController.openThirdPartyAppSync(
                        ThirdPartyAppAuthenticationPayload.of(TEST_SCA_REDIRECT_URL)))
                .thenReturn(Optional.empty());

        // when
        Throwable throwable =
                catchThrowable(
                        () -> consentRedirectAuthorizationStep.authorizeConsent(CONSENT_RESPONSE));

        // then
        assertThat(throwable)
                .isInstanceOf(ThirdPartyAppException.class)
                .extracting("error")
                .isEqualTo(ThirdPartyAppError.TIMED_OUT);
    }

    @Test
    public void shouldThrowCancelledWhenSupplementalInfoReturnedFromNotOkLink() {
        // given
        Map<String, String> suppInfoFromCallback = new HashMap<>();
        suppInfoFromCallback.put(QueryKeys.RESULT, QueryValues.FAILURE);

        when(mockSupplementalInformationController.openThirdPartyAppSync(
                        ThirdPartyAppAuthenticationPayload.of(TEST_SCA_REDIRECT_URL)))
                .thenReturn(Optional.of(suppInfoFromCallback));

        // when
        Throwable throwable =
                catchThrowable(
                        () -> consentRedirectAuthorizationStep.authorizeConsent(CONSENT_RESPONSE));

        // then
        assertThat(throwable)
                .isInstanceOf(ThirdPartyAppException.class)
                .extracting("error")
                .isEqualTo(ThirdPartyAppError.CANCELLED);
    }

    @Test
    public void shouldCheckUnderlyingConsentIfCallbackComesOkAndEndWithCancelledIfNotValid() {
        // given
        Map<String, String> suppInfoFromCallback = new HashMap<>();
        suppInfoFromCallback.put(QueryKeys.RESULT, QueryValues.SUCCESS);

        when(mockSupplementalInformationController.openThirdPartyAppSync(
                        ThirdPartyAppAuthenticationPayload.of(TEST_SCA_REDIRECT_URL)))
                .thenReturn(Optional.of(suppInfoFromCallback));

        when(mockAuthApiClient.fetchConsentStatus(TEST_CONSENT_ID))
                .thenReturn(
                        TestDataReader.readFromFile(
                                TestDataReader.CONSENT_STATUS_REJECTED,
                                CbiConsentStatusResponse.class));

        // when
        Throwable throwable =
                catchThrowable(
                        () -> consentRedirectAuthorizationStep.authorizeConsent(CONSENT_RESPONSE));

        // then
        assertThat(throwable)
                .isInstanceOf(ThirdPartyAppException.class)
                .extracting("error")
                .isEqualTo(ThirdPartyAppError.CANCELLED);
    }

    @Test
    public void shouldCompleteWithoutExceptionsWhenCallbackReturnedOkAndConsentValid() {
        // given
        Map<String, String> suppInfoFromCallback = new HashMap<>();
        suppInfoFromCallback.put(QueryKeys.RESULT, QueryValues.SUCCESS);

        when(mockSupplementalInformationController.openThirdPartyAppSync(
                        ThirdPartyAppAuthenticationPayload.of(TEST_SCA_REDIRECT_URL)))
                .thenReturn(Optional.of(suppInfoFromCallback));

        when(mockAuthApiClient.fetchConsentStatus(TEST_CONSENT_ID))
                .thenReturn(
                        TestDataReader.readFromFile(
                                TestDataReader.CONSENT_STATUS_VALID,
                                CbiConsentStatusResponse.class));

        // when & then
        assertThatCode(() -> consentRedirectAuthorizationStep.authorizeConsent(CONSENT_RESPONSE))
                .doesNotThrowAnyException();
    }
}
