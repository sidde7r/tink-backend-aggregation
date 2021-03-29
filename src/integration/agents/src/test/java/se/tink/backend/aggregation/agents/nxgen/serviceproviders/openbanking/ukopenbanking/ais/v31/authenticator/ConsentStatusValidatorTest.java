package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.entities.AccountPermissionsDataResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.rpc.AccountPermissionResponse;

public class ConsentStatusValidatorTest {

    private ConsentStatusValidator validator;

    private UkOpenBankingApiClient mockedApiClient;
    private AccountPermissionsDataResponseEntity mockedConsent;
    private AccountPermissionResponse mockedConsentResponse;
    private static final String DUMMY_CONSENT_ID = "DUMMY_CONSENT_ID";

    @Before
    public void setUp() throws Exception {
        this.mockedConsent = mock(AccountPermissionsDataResponseEntity.class);
        this.mockedConsentResponse = mock(AccountPermissionResponse.class);
        this.mockedApiClient = mock(UkOpenBankingApiClient.class);

        this.validator = new ConsentStatusValidator(mockedApiClient);
    }

    @Test
    public void shouldBeValid() {
        // given
        given(mockedConsentResponse.getData()).willReturn(mockedConsent);
        given(mockedApiClient.fetchIntentDetails(DUMMY_CONSENT_ID))
                .willReturn(mockedConsentResponse);

        // expected
        assertThat(validator.isInvalid(DUMMY_CONSENT_ID)).isFalse();
        verify(mockedApiClient, times(1)).fetchIntentDetails(any());
    }

    @Test
    public void shouldBeValidAfterAttempting3x() {
        // given
        final int MAX_ATTEMPTS = 5;

        given(mockedConsent.isAwaitingAuthorisation())
                .willReturn(Boolean.TRUE)
                .willReturn(Boolean.TRUE)
                .willReturn(Boolean.FALSE);

        given(mockedConsentResponse.getData()).willReturn(mockedConsent);
        given(mockedApiClient.fetchIntentDetails(DUMMY_CONSENT_ID))
                .willReturn(mockedConsentResponse);

        // expected
        assertThat(validator.isInvalidWithRetry(DUMMY_CONSENT_ID, MAX_ATTEMPTS)).isFalse();
        verify(mockedApiClient, times(3)).fetchIntentDetails(any());
    }

    @Test
    public void shouldBeInvalid() {
        // given
        given(mockedConsent.isNotAuthorised()).willReturn(Boolean.TRUE);

        given(mockedConsentResponse.getData()).willReturn(mockedConsent);
        given(mockedApiClient.fetchIntentDetails(DUMMY_CONSENT_ID))
                .willReturn(mockedConsentResponse);

        // expected
        assertThat(validator.isInvalid(DUMMY_CONSENT_ID)).isTrue();
        verify(mockedApiClient, times(1)).fetchIntentDetails(any());
    }

    @Test
    public void shouldBeInvalidAfterAttempting3x() {
        // given
        final int MAX_ATTEMPTS = 5;

        given(mockedConsent.isAwaitingAuthorisation())
                .willReturn(Boolean.TRUE)
                .willReturn(Boolean.TRUE)
                .willReturn(Boolean.FALSE);

        given(mockedConsent.isNotAuthorised()).willReturn(Boolean.TRUE);
        given(mockedConsentResponse.getData()).willReturn(mockedConsent);
        given(mockedApiClient.fetchIntentDetails(DUMMY_CONSENT_ID))
                .willReturn(mockedConsentResponse);

        // expected
        assertThat(validator.isInvalidWithRetry(DUMMY_CONSENT_ID, MAX_ATTEMPTS)).isTrue();
        verify(mockedApiClient, times(3)).fetchIntentDetails(any());
    }

    @Test
    public void shouldBeInvalidAfterExceedingMaxAttempts() {
        // given
        final int MAX_ATTEMPTS = 3;

        given(mockedConsent.isNotAuthorised()).willReturn(Boolean.TRUE);
        given(mockedConsent.isAwaitingAuthorisation()).willReturn(Boolean.TRUE);

        given(mockedConsentResponse.getData()).willReturn(mockedConsent);
        given(mockedApiClient.fetchIntentDetails(DUMMY_CONSENT_ID))
                .willReturn(mockedConsentResponse);

        // expected
        assertThat(validator.isInvalidWithRetry(DUMMY_CONSENT_ID, MAX_ATTEMPTS)).isTrue();
        verify(mockedApiClient, times(3)).fetchIntentDetails(any());
    }
}
