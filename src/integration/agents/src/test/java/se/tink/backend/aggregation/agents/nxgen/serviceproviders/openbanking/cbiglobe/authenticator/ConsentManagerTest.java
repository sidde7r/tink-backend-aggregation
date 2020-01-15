package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.ConsentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.GetAccountsResponse;

public class ConsentManagerTest {

    private static final String STATE = "state";
    private static final String CONSENT_ID = "123";
    private ConsentManager consentManager;
    private CbiGlobeApiClient apiClient;
    private CbiUserState userState;

    @Before
    public void init() {
        apiClient = Mockito.mock(CbiGlobeApiClient.class);
        userState = Mockito.mock(CbiUserState.class);
        consentManager = new ConsentManager(apiClient, userState);
    }

    @Test
    public void accountConsentValidPeriodShouldBe89Days() {
        // when
        ConsentRequest consentRequest = consentManager.createConsentRequestAccount();

        // then
        LocalDate localDate = LocalDate.parse(consentRequest.getValidUntil());
        assertThat(localDate).isEqualTo(LocalDate.now().plusDays(89));
    }

    @Test
    public void createAccountConsentShouldCallApiClientAndStartManualAuthentication() {
        // given
        ConsentResponse consentResponse = new ConsentResponse(null, CONSENT_ID, null);
        when(apiClient.createConsent(eq(STATE), eq(ConsentType.ACCOUNT), any()))
                .thenReturn(consentResponse);

        // when
        consentManager.createAccountConsent(STATE);

        // then
        verify(apiClient, times(1)).createConsent(eq(STATE), eq(ConsentType.ACCOUNT), any());
        verify(userState, times(1)).startManualAuthenticationStep(CONSENT_ID);
    }

    @Test
    public void transactionsConsentValidPeriodShouldBe89Days() {
        // given
        GetAccountsResponse getAccountsResponse =
                new GetAccountsResponse(Collections.singletonList(new AccountEntity("123")));

        // when
        ConsentRequest consentRequest =
                consentManager.createConsentRequestBalancesTransactions(getAccountsResponse);

        // then
        LocalDate localDate = LocalDate.parse(consentRequest.getValidUntil());
        assertThat(localDate).isEqualTo(LocalDate.now().plusDays(89));
    }

    @Test
    public void createTransactionsConsentShouldCallApiClientAndStartManualAuthentication() {
        // given
        ConsentResponse consentResponse = new ConsentResponse(null, CONSENT_ID, null);
        when(apiClient.fetchAccounts())
                .thenReturn(
                        new GetAccountsResponse(
                                Collections.singletonList(new AccountEntity("123"))));
        when(apiClient.createConsent(eq(STATE), eq(ConsentType.BALANCE_TRANSACTION), any()))
                .thenReturn(consentResponse);

        // when
        consentManager.createTransactionsConsent(STATE);

        // then
        verify(apiClient, times(1))
                .createConsent(eq(STATE), eq(ConsentType.BALANCE_TRANSACTION), any());
        verify(userState, times(1)).startManualAuthenticationStep(CONSENT_ID);
    }

    @Test
    public void isConsentAcceptedShouldNotThrowExceptionIfAccepted() throws SessionException {
        // given
        when(apiClient.getConsentStatus(StorageKeys.CONSENT_ID)).thenReturn(ConsentStatus.VALID);

        // when
        boolean isAccepted = consentManager.isConsentAccepted();

        // then
        assertThat(isAccepted).isEqualTo(true);
    }

    @Test
    public void isConsentAcceptedShouldThrowExceptionIfNotAccepted() throws SessionException {
        // given
        when(apiClient.getConsentStatus(StorageKeys.CONSENT_ID)).thenReturn(ConsentStatus.REJECTED);

        // when
        Throwable thrown = catchThrowable(() -> consentManager.isConsentAccepted());

        // then
        verify(userState, times(1)).resetAuthenticationState();

        Assertions.assertThat(thrown).isInstanceOf(SessionException.class);
    }

    @Test
    public void updateAuthenticationMethodShouldThrowExceptionIfGetConsentStatusThrowException()
            throws SessionException {
        // given
        when(apiClient.getConsentStatus(StorageKeys.CONSENT_ID)).thenThrow(SessionException.class);
        when(userState.getChosenAuthenticationMethodId()).thenReturn("1");
        when(userState.getConsentId()).thenReturn(CONSENT_ID);

        // when
        consentManager.updateAuthenticationMethod();

        // then
        verify(apiClient, times(1)).updateConsent(eq(CONSENT_ID), any());
    }
}
