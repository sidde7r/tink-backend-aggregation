package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.configuration.AspspConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.ConsentController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.entities.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.rpc.BaseTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

@RequiredArgsConstructor
public class RedsysTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionFetcher<TransactionalAccount>,
                UpcomingTransactionFetcher<TransactionalAccount> {
    private final RedsysApiClient apiClient;
    private final ConsentController consentController;
    private final AspspConfiguration aspspConfiguration;
    private final TransactionPaginationHelper paginationHelper;

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
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        List<AggregationTransaction> transactions = new ArrayList<>();

        if (aspspConfiguration.supportsPendingTransactions()) {
            transactions.addAll(fetchUpcomingTransactionsFor(account));
        }

        LocalDate dateOfLastFetchedTransactions =
                paginationHelper
                        .getTransactionDateLimit(account)
                        .map(date -> toLocaDate(date))
                        .orElse(null);

        final String consentId = consentController.getConsentId();

        BaseTransactionsResponse response =
                apiClient.fetchTransactions(
                        account.getApiIdentifier(), consentId, dateOfLastFetchedTransactions);
        transactions.addAll(response.getTinkTransactions());
        while (response.nextKey() != null) {
            response = apiClient.fetchTransactionsWithKey(response.nextKey(), consentId);
            transactions.addAll(response.getTinkTransactions());
        }

        return transactions;
    }

    private LocalDate toLocaDate(Date date) {
        return new java.sql.Date(date.getTime()).toLocalDate();
    }

    @Override
    public Collection<UpcomingTransaction> fetchUpcomingTransactionsFor(
            TransactionalAccount account) {
        final BaseTransactionsResponse response =
                apiClient.fetchPendingTransactions(
                        account.getApiIdentifier(), consentController.getConsentId());
        return response.getUpcomingTransactions();
    }
}
