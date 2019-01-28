package se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.LaBanquePostaleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.agents.rpc.AccountTypes;

public class LaBanquePostaleTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount>,
        TransactionPagePaginator<TransactionalAccount> {

    private final LaBanquePostaleApiClient apiClient;

    public LaBanquePostaleTransactionalAccountFetcher(
            LaBanquePostaleApiClient apiClient) {
        this.apiClient = apiClient;
    }

    private static boolean isTransactionalAccount(AccountEntity accountEntity) {
        AccountTypes tinkType = accountEntity.toTinkAccountType();
        return AccountTypes.CHECKING.equals(tinkType) || AccountTypes.SAVINGS.equals(tinkType);
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {

        AccountsResponse response = apiClient.getAccounts();

        return response.getAccounts().stream()
                .filter(LaBanquePostaleTransactionalAccountFetcher::isTransactionalAccount)
                .map(AccountEntity::toTinkAccount)
                .collect(Collectors.toList());

    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {

        TransactionsResponse response = apiClient.getTransactions(account.getBankIdentifier(), account.getType());

        List<Transaction> transactions = response.getTransactions().stream().map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());

        return PaginatorResponseImpl.create(transactions, false);
    }

}
