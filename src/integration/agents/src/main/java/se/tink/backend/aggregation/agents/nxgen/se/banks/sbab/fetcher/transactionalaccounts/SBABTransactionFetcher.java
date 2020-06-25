package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccounts;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SBABConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccounts.entities.TransactionsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SBABTransactionFetcher implements TransactionFetcher<TransactionalAccount> {
    private final SessionStorage sessionStorage;

    public SBABTransactionFetcher(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        final AccountsResponse accountsResponse =
                sessionStorage
                        .get(StorageKeys.ACCOUNT_RESPONSE, AccountsResponse.class)
                        .orElse(new AccountsResponse());

        return accountsResponse.getAccounts().getPersonalAccounts().stream()
                .filter(p -> p.getAccountNumber().equals(account.getAccountNumber()))
                .map(p -> p.getTransfers().getTransactions())
                .flatMap(List::stream)
                .map(TransactionsEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }
}
