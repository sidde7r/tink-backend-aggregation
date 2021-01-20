package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.EndUserMessage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.TimeValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.rpc.StatementResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.rpc.GenericResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@Slf4j
@JsonObject
public class SwedbankTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final SwedbankApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;
    private FetchAccountResponse fetchAccountResponse;

    public SwedbankTransactionalAccountFetcher(
            SwedbankApiClient apiClient,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        handleConsentFlow();
        // Request Creation of the transactions over 90 days zip file in advance
        getAccounts().getAccountList().forEach(this::postAccountStatement);
        return getAccounts().getAccountList().stream()
                .map(toTinkAccountWithBalance())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Function<AccountEntity, Optional<TransactionalAccount>> toTinkAccountWithBalance() {
        return account -> {
            if (account.getBalances() != null && !account.getBalances().isEmpty()) {
                return account.toTinkAccount(account.getBalances());
            } else {
                return account.toTinkAccount(
                        apiClient.getAccountBalance(account.getResourceId()).getBalances());
            }
        };
    }

    private void handleConsentFlow() {
        try {
            if (apiClient.isConsentValid()) {
                return;
            }
        } catch (HttpResponseException e) {
            handleFetchAccountError(e);
        }

        useConsent(apiClient.getConsentAllAccounts().getConsentId());
        getDetailedConsent(getAccounts())
                .ifPresent(consentResponse -> useConsent(consentResponse.getConsentId()));
    }

    private FetchAccountResponse getAccounts() {
        try {
            if (fetchAccountResponse == null) {
                fetchAccountResponse = apiClient.fetchAccounts();
            }
            return fetchAccountResponse;
        } catch (HttpResponseException e) {
            handleFetchAccountError(e);
            throw e;
        }
    }

    private Optional<ConsentResponse> getDetailedConsent(
            FetchAccountResponse fetchAccountResponse) {

        return fetchAccountResponse.getAccountList().isEmpty()
                ? Optional.empty()
                : Optional.of(
                        apiClient.getConsentAccountDetails(
                                mapAccountResponseToIbanList(fetchAccountResponse)));
    }

    private List<String> mapAccountResponseToIbanList(FetchAccountResponse accounts) {
        return accounts.getAccountList().stream()
                .map(AccountEntity::getIban)
                .collect(Collectors.toList());
    }

    private void handleFetchAccountError(HttpResponseException e) {
        GenericResponse errorResponse = e.getResponse().getBody(GenericResponse.class);

        if (errorResponse.isConsentInvalid()
                || errorResponse.isResourceUnknown()
                || errorResponse.isConsentExpired()) {
            removeConsent();
        }

        if (errorResponse.isKycError() || errorResponse.isMissingBankAgreement()) {
            throw AuthorizationError.ACCOUNT_BLOCKED.exception(
                    EndUserMessage.MUST_UPDATE_AGREEMENT.getKey());
        }
    }

    private void useConsent(String consentId) {
        persistentStorage.put(SwedbankConstants.StorageKeys.CONSENT, consentId);
    }

    private void removeConsent() {
        // Use the consent ID for communication with Swedbank
        log.info(
                "Remvoving invalid consent with ID = {}",
                persistentStorage.get(SwedbankConstants.StorageKeys.CONSENT));
        persistentStorage.remove(SwedbankConstants.StorageKeys.CONSENT);
    }

    private void postAccountStatement(AccountEntity accountEntity) {
        HttpResponse response =
                apiClient.getTransactions(
                        accountEntity.getResourceId(),
                        Timestamp.valueOf(
                                LocalDateTime.now()
                                        .minusMonths(TimeValues.MONTHS_TO_FETCH_MAX)),
                        Timestamp.valueOf(LocalDateTime.now()));
        sessionStorage.put(
                accountEntity.getResourceId(), response.getBody(StatementResponse.class));
    }
}
