package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities.OpBankAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities.OpBankTransactionPaginationKey;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class OpBankTransactionalAccountsFetcher implements AccountFetcher<TransactionalAccount>,
        TransactionKeyPaginator<TransactionalAccount, OpBankTransactionPaginationKey> {

    private final OpBankApiClient client;

    public OpBankTransactionalAccountsFetcher(OpBankApiClient client) {
        this.client = client;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return client.fetchAccounts().getAccounts().stream()
                .filter(OpBankAccountEntity::isTransactionalAccount)
                .map(OpBankAccountEntity::toTransactionalAccount)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionKeyPaginatorResponse<OpBankTransactionPaginationKey> getTransactionsFor(TransactionalAccount account,
            OpBankTransactionPaginationKey nextKey) {
        if (nextKey == null) {
            return client.getTransactions(account);
        } else {
            return client.getTransactions(account, nextKey);
        }
    }
}
