package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.configuration.AspspConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.RedsysConsentController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.entities.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.entities.PaginationKey;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class RedsysTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionKeyPaginator<TransactionalAccount, PaginationKey> {
    private final RedsysApiClient apiClient;
    private final RedsysConsentController consentController;
    private final AspspConfiguration aspspConfiguration;

    public RedsysTransactionalAccountFetcher(
            RedsysApiClient apiClient,
            RedsysConsentController consentController,
            AspspConfiguration aspspConfiguration) {
        this.apiClient = apiClient;
        this.consentController = consentController;
        this.aspspConfiguration = aspspConfiguration;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        final String consentId = consentController.getConsentId();
        ListAccountsResponse accountsResponse = apiClient.fetchAccounts(consentId);
        return accountsResponse.getAccounts().stream()
                .map(this::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<TransactionalAccount> toTinkAccount(AccountEntity account) {
        final List<BalanceEntity> accountBalances;
        if (account.hasBalances()) {
            accountBalances = account.getBalances();
        } else {
            final String accountId = account.getResourceId();
            final String consentId = consentController.getConsentId();
            accountBalances = apiClient.fetchAccountBalances(accountId, consentId).getBalances();
        }
        return account.toTinkAccount(accountBalances, aspspConfiguration);
    }

    @Override
    public TransactionKeyPaginatorResponse<PaginationKey> getTransactionsFor(
            TransactionalAccount account, PaginationKey key) {
        try {
            final String consentId = consentController.getConsentId();
            return apiClient.fetchTransactions(account.getApiIdentifier(), consentId, key);
        } catch (HttpResponseException hre) {
            final ErrorResponse error = ErrorResponse.fromResponse(hre.getResponse());
            if (error.hasErrorCode(ErrorCodes.CONSENT_EXPIRED) && Objects.isNull(key)) {
                // Request new consent
                if (!consentController.requestConsent()) {
                    consentController.clearConsentStorage();
                    throw SessionError.CONSENT_REVOKED.exception();
                }
                final String consentId = consentController.getConsentId();
                // Server will return 500 if accounts aren't fetched first with this consent
                // (Bankinter)
                apiClient.fetchAccounts(consentId);
                return apiClient.fetchTransactions(account.getApiIdentifier(), consentId, key);
            }
            throw hre;
        }
    }
}
