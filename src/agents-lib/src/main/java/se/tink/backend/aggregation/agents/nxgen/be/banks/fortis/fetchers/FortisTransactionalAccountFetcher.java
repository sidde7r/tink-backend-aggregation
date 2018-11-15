package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.FortisApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.FortisConstants;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class FortisTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount>,
        TransactionKeyPaginator<TransactionalAccount, String>, UpcomingTransactionFetcher<TransactionalAccount> {

    private static final AggregationLogger LOGGER = new AggregationLogger(
            FortisTransactionalAccountFetcher.class);

    private final FortisApiClient apiClient;

    public FortisTransactionalAccountFetcher(FortisApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(TransactionalAccount account, String key) {
        return new TransactionKeyPaginatorResponseImpl<>();
    }

    @Override
    public Collection<UpcomingTransaction> fetchUpcomingTransactionsFor(TransactionalAccount account) {
        Collection<UpcomingTransaction> upcomingTransactions = Lists.newArrayList();

        return upcomingTransactions;
    }
}
