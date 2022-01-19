package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.CommerzBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.TestDataReader;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.StorageKeys;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthorizationStatusResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class CommerzBankDecoupledPaymentAuthenticatorTest {

    private static final int TEST_POLL_ATTEMPTS = 3;
    private static final long TEST_DELAY = 5L;

    private static final String SCA_URL = "https://example.com/sca/url";

    private static final AuthorizationStatusResponse SCA_STARTED =
            TestDataReader.readFromFile(
                    TestDataReader.SCA_STATUS_STARTED, AuthorizationStatusResponse.class);
    private static final AuthorizationStatusResponse SCA_FAILED =
            TestDataReader.readFromFile(
                    TestDataReader.SCA_STATUS_FAILED, AuthorizationStatusResponse.class);
    private static final AuthorizationStatusResponse SCA_EXEMPTED =
            TestDataReader.readFromFile(
                    TestDataReader.SCA_STATUS_EXEMPTED, AuthorizationStatusResponse.class);
    private static final AuthorizationStatusResponse SCA_FINALISED =
            TestDataReader.readFromFile(
                    TestDataReader.SCA_STATUS_FINALISED, AuthorizationStatusResponse.class);

    private CommerzBankApiClient mockApiClient;
    private SupplementalInformationController mockController;

    private CommerzBankDecoupledPaymentAuthenticator decoupledPaymentAuthenticator;

    @Before
    public void setup() {

        mockApiClient = mock(CommerzBankApiClient.class);
        SessionStorage sessionStorage = new SessionStorage();
        sessionStorage.put(StorageKeys.SCA_STATUS_LINK, SCA_URL);
        mockController = mock(SupplementalInformationController.class);
        SupplementalInformationFormer mockSupplementalInformationFormer =
                mock(SupplementalInformationFormer.class);

        decoupledPaymentAuthenticator =
                new CommerzBankDecoupledPaymentAuthenticator(
                        mockApiClient,
                        sessionStorage,
                        mockController,
                        mockSupplementalInformationFormer,
                        TEST_POLL_ATTEMPTS,
                        TEST_DELAY);
    }

    @Test
    public void shouldDisplaySupplementalInfoAndPollForStatusAndEndWithNoExceptionWhenSuccesful() {
        // given
        when(mockApiClient.fetchAuthorizationStatus(SCA_URL))
                .thenReturn(SCA_STARTED, SCA_STARTED, SCA_FINALISED);

        // when & then
        assertThatCode(decoupledPaymentAuthenticator::authenticate).doesNotThrowAnyException();
        verify(mockController).askSupplementalInformationSync(any());
    }

    @Test
    public void shouldDisplaySupplementalInfoAndPollForStatusAndEndWithNoExceptionWhenExempted() {
        // given
        when(mockApiClient.fetchAuthorizationStatus(SCA_URL))
                .thenReturn(SCA_STARTED, SCA_STARTED, SCA_EXEMPTED);

        // when & then
        assertThatCode(decoupledPaymentAuthenticator::authenticate).doesNotThrowAnyException();
        verify(mockController).askSupplementalInformationSync(any());
    }

    @Test
    public void shouldDisplaySupplementalInfoAndPollForStatusAndEndWithCancelledWhenFailed() {
        // given
        when(mockApiClient.fetchAuthorizationStatus(SCA_URL))
                .thenReturn(SCA_STARTED, SCA_STARTED, SCA_FAILED);

        // when
        Throwable throwable = catchThrowable(() -> decoupledPaymentAuthenticator.authenticate());

        // then
        assertThat(throwable)
                .isInstanceOf(ThirdPartyAppException.class)
                .hasMessage("Cause: ThirdPartyAppError.CANCELLED");
        verify(mockController).askSupplementalInformationSync(any());
    }

    @Test
    public void shouldDisplaySupplementalInfoAndPollForStatusAndEndWithTimeoutWhenNoFinalStatus() {
        // given
        when(mockApiClient.fetchAuthorizationStatus(SCA_URL))
                .thenReturn(SCA_STARTED, SCA_STARTED, SCA_STARTED);

        // when
        Throwable throwable = catchThrowable(() -> decoupledPaymentAuthenticator.authenticate());

        // then
        assertThat(throwable)
                .isInstanceOf(ThirdPartyAppException.class)
                .hasMessage("Cause: ThirdPartyAppError.TIMED_OUT");
        verify(mockController).askSupplementalInformationSync(any());
    }
}
