package se.tink.backend.aggregation.agents.nxgen.uk.banks.monzo.fetcher.transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.monzo.MonzoApiClient;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.monzo.MonzoConstants;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.monzo.fetcher.transactional.entity.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.AccountTypes;

public class MonzoTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>, TransactionKeyPaginator<TransactionalAccount, String> {

    private final MonzoApiClient apiClient;

    public MonzoTransactionalAccountFetcher(MonzoApiClient client) {
        apiClient = client;
    }

    @Override
    public List<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchAccounts()
                .getAccounts()
                .stream()
                .filter(entity -> MonzoConstants.AccountType.verify(entity.getType(), AccountTypes.CHECKING))
                .map(AccountEntity::toTinkAccount)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(TransactionalAccount account, String key) {
        return apiClient.fetchTransactions(account.getBankIdentifier(), key, Instant.now());
    }
}
