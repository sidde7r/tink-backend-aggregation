package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.transactionalaccount;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.OmaspApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.transactionalaccount.entities.AccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.transactionalaccount.entities.TransactionsEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.transactionalaccount.rpc.TransactionDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Credentials;


public class OmaspTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount>, TransactionPagePaginator<TransactionalAccount> {
    private static final AggregationLogger LOGGER = new AggregationLogger(OmaspTransactionalAccountFetcher.class);

    private final OmaspApiClient apiClient;
    private final Credentials credentials;

    private final Map<TransactionalAccount, List<TransactionsEntity>> accountTransactions = new HashMap<>();

    public OmaspTransactionalAccountFetcher(OmaspApiClient apiClient, Credentials credentials) {
        this.apiClient = apiClient;
        this.credentials = credentials;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        this.accountTransactions.clear();
        List<AccountsEntity> accounts = this.apiClient.getAccounts();

        // Must fetch transaction list for the account in order to get the account type
        accounts.forEach(account -> {
            TransactionsResponse transactionsResponse = this.apiClient.getTransactionsFor(account.getId());

            AccountTypes accountType = transactionsResponse.getTinkAccountType(this.credentials);

            this.accountTransactions.put(account.toTransactionalAccount(accountType),
                    transactionsResponse.getTransactions());
        });

        return new ArrayList<>(this.accountTransactions.keySet());
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {

        List<TransactionsEntity> transactionsEntities = this.accountTransactions.getOrDefault(account, null);
        if (transactionsEntities == null) {
            // should not happen
            return PaginatorResponseImpl.createEmpty(false);
        }

        if (transactionsEntities.size() <= page) {
            return PaginatorResponseImpl.createEmpty(false);
        }

        TransactionsEntity transactionsEntity = transactionsEntities.get(page);
        TransactionDetailsResponse transactionDetailsResponse = this.apiClient.getTransactionDetails(
                account.getBankIdentifier(), transactionsEntity.getId());

        return PaginatorResponseImpl.create(Collections.singletonList(transactionDetailsResponse.toTinkTransaction()),
                page+1 < transactionsEntities.size());
    }
}
