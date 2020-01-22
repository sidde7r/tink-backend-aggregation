package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.AccountDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.ConsentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.MessageCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.UpdateConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class ConsentManager {

    private final CbiGlobeApiClient apiClient;
    private final CbiUserState userState;

    public ConsentManager(CbiGlobeApiClient apiClient, CbiUserState userState) {
        this.apiClient = apiClient;
        this.userState = userState;
    }

    public ConsentResponse createAccountConsent(String state) {
        ConsentRequest consentRequestAccount = createConsentRequestAccount();
        return create(state, ConsentType.ACCOUNT, consentRequestAccount);
    }

    public ConsentResponse createTransactionsConsent(String state) {
        GetAccountsResponse getAccountsResponse = apiClient.fetchAccounts();
        ConsentRequest consentRequestBalancesTransactions =
                createConsentRequestBalancesTransactions(getAccountsResponse);

        return create(state, ConsentType.BALANCE_TRANSACTION, consentRequestBalancesTransactions);
    }

    ConsentRequest createConsentRequestAccount() {
        return new ConsentRequest(
                new AccessEntity(FormValues.ALL_ACCOUNTS),
                FormValues.TRUE,
                FormValues.FREQUENCY_PER_DAY_ONE,
                FormValues.FALSE,
                LocalDate.now().plusDays(FormValues.CONSENT_VALID_PERIOD_DAYS).toString());
    }

    ConsentRequest createConsentRequestBalancesTransactions(
            GetAccountsResponse getAccountsResponse) {
        List<AccountDetailsEntity> accountDetailsEntities =
                getAccountsResponse.getAccounts().stream()
                        .map(acc -> new AccountDetailsEntity(acc.getResourceId(), acc.getIban()))
                        .collect(Collectors.toList());

        return new ConsentRequest(
                new AccessEntity(accountDetailsEntities, accountDetailsEntities),
                FormValues.TRUE,
                FormValues.FREQUENCY_PER_DAY,
                FormValues.TRUE,
                LocalDate.now().plusDays(FormValues.CONSENT_VALID_PERIOD_DAYS).toString());
    }

    private ConsentResponse create(
            String state, ConsentType consentType, ConsentRequest consentRequest) {
        ConsentResponse consentResponse =
                apiClient.createConsent(state, consentType, consentRequest);
        userState.startManualAuthenticationStep(consentResponse.getConsentId());

        return consentResponse;
    }

    boolean isConsentAccepted() throws SessionException {
        ConsentStatus consentStatus;
        try {
            consentStatus = apiClient.getConsentStatus(StorageKeys.CONSENT_ID);
        } catch (HttpResponseException e) {
            handleInvalidConsents(e);
            throw e;
        }

        if (!consentStatus.isAcceptedStatus()) {
            userState.resetAuthenticationState();
            throw SessionError.SESSION_EXPIRED.exception();
        }

        return true;
    }

    private void handleInvalidConsents(HttpResponseException e) throws SessionException {
        final String message = e.getResponse().getBody(String.class);
        if (isConsentsProblem(message)) {
            userState.resetAuthenticationState();
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    private boolean isConsentsProblem(String message) {
        return message.contains(MessageCodes.CONSENT_INVALID.name())
                || message.contains(MessageCodes.CONSENT_EXPIRED.name())
                || message.contains(MessageCodes.RESOURCE_UNKNOWN.name());
    }

    public ConsentResponse updateAuthenticationMethod() {
        String chosenMethodId = userState.getChosenAuthenticationMethodId();
        return updateAuthenticationMethod(chosenMethodId);
    }

    public ConsentResponse updateAuthenticationMethod(String authenticationMethodId) {
        String consentId = userState.getConsentId();
        UpdateConsentRequest body = new UpdateConsentRequest(authenticationMethodId);

        return apiClient.updateConsent(consentId, body);
    }
}
