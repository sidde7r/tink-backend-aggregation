package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.FortisApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.FortisConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.rpc.TransactionsResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class FortisTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount>,
        TransactionPagePaginator<TransactionalAccount>, UpcomingTransactionFetcher<TransactionalAccount> {

    private static final AggregationLogger LOGGER = new AggregationLogger(
            FortisTransactionalAccountFetcher.class);

    private final FortisApiClient apiClient;

    public FortisTransactionalAccountFetcher(FortisApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return this.apiClient.fetchAccounts().toTinkAccounts();
    }

    @Override
    public Collection<UpcomingTransaction> fetchUpcomingTransactionsFor(TransactionalAccount account) {
        Collection<UpcomingTransaction> upcomingTransactions = Lists.newArrayList();

        return upcomingTransactions;
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {
        try {
            String accountProductId = account.getFromTemporaryStorage(FortisConstants.STORAGE.ACCOUNT_PRODUCT_ID);

            if (Strings.isNullOrEmpty(accountProductId)) {
                throw new IllegalStateException("Missing accountproductID!");
            }

            TransactionsResponse res = this.apiClient.fetchTransactions(page, accountProductId);
            this.apiClient.fetchAndLogUpcoming(page, accountProductId);
            return res;
        } catch (HttpResponseException hre) { // TODO: add logging
            return PaginatorResponseImpl.createEmpty(false);
        }

    }
}
