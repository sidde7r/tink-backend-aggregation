package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.ConsentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.CredentialsDetailRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.CredentialsDetailResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.PsuCredentialsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.PsuCredentialsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.UpdateConsentPsuCredentialsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.GetAccountsResponse;

public class ConsentManagerTest {

    private static final String STATE = "state";
    private static final String CONSENT_ID = "123";

    private static final String ASPSP_PRODUCT_CODE = "aspspProductCode";
    private static final String USER_KEY = "USER_KEY";
    private static final String PASSWORD_KEY = "PASSWORD_KEY";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private ConsentManager consentManager;
    private CbiGlobeApiClient apiClient;
    private CbiUserState userState;

    @Before
    public void init() {
        apiClient = Mockito.mock(CbiGlobeApiClient.class);
        userState = Mockito.mock(CbiUserState.class);
        consentManager = new ConsentManager(apiClient, userState, 100L, 3);
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

    @Test
    public void updatePsuCredentialsShouldInvokeApiClientWithProperValues() {
        // given
        UpdateConsentPsuCredentialsRequest updateConsentPsuCredentialsRequest =
                createUpdateConsentPsuCredentialsRequest();

        List<CredentialsDetailResponse> credentialsDetails =
                Arrays.asList(
                        new CredentialsDetailResponse(USER_KEY, false),
                        new CredentialsDetailResponse(PASSWORD_KEY, true));

        PsuCredentialsResponse psuCredentialsResponse =
                new PsuCredentialsResponse(ASPSP_PRODUCT_CODE, credentialsDetails);

        when(userState.getConsentId()).thenReturn(CONSENT_ID);

        // when
        consentManager.updatePsuCredentials(USERNAME, PASSWORD, psuCredentialsResponse);

        // then
        verify(apiClient, times(1))
                .updateConsentPsuCredentials(
                        eq(CONSENT_ID), eq(updateConsentPsuCredentialsRequest));
    }

    @Test
    public void updatePsuCredentialsShouldThrowExceptionIfNoSecretAmongCredentials() {
        // given
        List<CredentialsDetailResponse> credentialsDetails =
                Arrays.asList(
                        new CredentialsDetailResponse(USER_KEY, false),
                        new CredentialsDetailResponse(PASSWORD_KEY, false));

        PsuCredentialsResponse psuCredentialsResponse =
                new PsuCredentialsResponse(ASPSP_PRODUCT_CODE, credentialsDetails);

        when(userState.getConsentId()).thenReturn(CONSENT_ID);

        // when
        Throwable thrown =
                catchThrowable(
                        () ->
                                consentManager.updatePsuCredentials(
                                        USERNAME, PASSWORD, psuCredentialsResponse));

        // then
        verify(apiClient, times(0)).updateConsentPsuCredentials(eq(CONSENT_ID), any());

        Assertions.assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void updatePsuCredentialsShouldThrowExceptionIfMoreThanOneSecretAmongCredentials() {
        // given
        List<CredentialsDetailResponse> credentialsDetails =
                Arrays.asList(
                        new CredentialsDetailResponse(USER_KEY, true),
                        new CredentialsDetailResponse(PASSWORD_KEY, true));

        PsuCredentialsResponse psuCredentialsResponse =
                new PsuCredentialsResponse(ASPSP_PRODUCT_CODE, credentialsDetails);

        when(userState.getConsentId()).thenReturn(CONSENT_ID);

        // when
        Throwable thrown =
                catchThrowable(
                        () ->
                                consentManager.updatePsuCredentials(
                                        USERNAME, PASSWORD, psuCredentialsResponse));

        // then
        verify(apiClient, times(0)).updateConsentPsuCredentials(eq(CONSENT_ID), any());

        Assertions.assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void updatePsuCredentialsShouldThrowExceptionIfPsuCredentialsNull() {
        // given
        when(userState.getConsentId()).thenReturn(CONSENT_ID);

        // when
        Throwable thrown =
                catchThrowable(() -> consentManager.updatePsuCredentials(USERNAME, PASSWORD, null));

        // then
        verify(apiClient, times(0)).updateConsentPsuCredentials(eq(CONSENT_ID), any());

        Assertions.assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    private UpdateConsentPsuCredentialsRequest createUpdateConsentPsuCredentialsRequest() {
        return new UpdateConsentPsuCredentialsRequest(
                new PsuCredentialsRequest(
                        ASPSP_PRODUCT_CODE,
                        Arrays.asList(
                                new CredentialsDetailRequest(USER_KEY, USERNAME),
                                new CredentialsDetailRequest(PASSWORD_KEY, PASSWORD))));
    }

    @Test
    public void waitForAcceptanceShouldRetryIfConsentPending() throws AuthenticationException {
        // given
        when(apiClient.getConsentStatus(StorageKeys.CONSENT_ID))
                .thenReturn(ConsentStatus.RECEIVED)
                .thenReturn(ConsentStatus.VALID);

        // when
        consentManager.waitForAcceptance();

        // then
        verify(apiClient, times(2)).getConsentStatus(StorageKeys.CONSENT_ID);
    }

    @Test
    public void waitForAcceptanceShouldThrowExceptionIfConsentRejected()
            throws AuthenticationException {
        // given
        when(apiClient.getConsentStatus(StorageKeys.CONSENT_ID)).thenReturn(ConsentStatus.REJECTED);

        // when
        Throwable thrown = catchThrowable(consentManager::waitForAcceptance);

        // then
        verify(apiClient, times(1)).getConsentStatus(StorageKeys.CONSENT_ID);
        Assertions.assertThat(thrown).isInstanceOf(LoginException.class);
    }

    @Test
    public void waitForAcceptanceShouldThrowExceptionIfRetryLimitReached()
            throws AuthenticationException {
        // given
        when(apiClient.getConsentStatus(StorageKeys.CONSENT_ID))
                .thenReturn(ConsentStatus.RECEIVED)
                .thenReturn(ConsentStatus.RECEIVED)
                .thenReturn(ConsentStatus.RECEIVED);

        // when
        Throwable thrown = catchThrowable(consentManager::waitForAcceptance);

        // then
        verify(apiClient, times(3)).getConsentStatus(StorageKeys.CONSENT_ID);
        Assertions.assertThat(thrown).isInstanceOf(LoginException.class);
    }
}
