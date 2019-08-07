package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.RedsysConsentController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.entities.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class RedsysTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionKeyPaginator<TransactionalAccount, String> {

    private final RedsysApiClient apiClient;
    private final RedsysConsentController consentController;

    public RedsysTransactionalAccountFetcher(
            RedsysApiClient apiClient, RedsysConsentController consentController) {
        this.apiClient = apiClient;
        this.consentController = consentController;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        consentController.requestConsentIfNeeded();
        final String consentId = consentController.getConsentId();
        ListAccountsResponse accountsResponse = apiClient.fetchAccounts(consentId);
        return accountsResponse.getAccounts().stream()
                .map(this::toTinkAccount)
                .collect(Collectors.toList());
    }

    private TransactionalAccount toTinkAccount(AccountEntity account) {
        final List<BalanceEntity> accountBalances;
        if (account.hasBalances()) {
            accountBalances = account.getBalances();
        } else {
            final String accountId = account.getResourceId();
            final String consentId = consentController.getConsentId();
            accountBalances = apiClient.fetchAccountBalances(accountId, consentId).getBalances();
        }
        return account.toTinkAccount(accountBalances);
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {
        consentController.requestConsentIfNeeded();
        final String consentId = consentController.getConsentId();
        return apiClient.fetchTransactions(account.getApiIdentifier(), consentId, key);
    }
}
