package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.FortisApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.FortisConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.rpc.UpcomingTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class FortisTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionPagePaginator<TransactionalAccount>,
                UpcomingTransactionFetcher<TransactionalAccount> {

    private final FortisApiClient apiClient;

    public FortisTransactionalAccountFetcher(FortisApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchAccounts().toTinkAccounts().stream()
                .filter(this::onlySavings)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<UpcomingTransaction> fetchUpcomingTransactionsFor(
            TransactionalAccount account) {
        int page = 1;
        ArrayList<UpcomingTransaction> upcomingTransactions = Lists.newArrayList();
        try {
            String accountProductId =
                    account.getFromTemporaryStorage(FortisConstants.Storage.ACCOUNT_PRODUCT_ID);

            verifyProductId(accountProductId);
            UpcomingTransactionsResponse upcomingTransactionsResponse;
            do {
                upcomingTransactionsResponse =
                        apiClient.fetchUpcomingTransactions(page, accountProductId);
                upcomingTransactions.addAll(upcomingTransactionsResponse.getTinkTransactions());
                page = page + 1;
            } while (upcomingTransactionsResponse.canFetchMore().isPresent()
                    && upcomingTransactionsResponse.canFetchMore().get());
            return upcomingTransactions;
        } catch (HttpResponseException hre) {
            return upcomingTransactions;
        }
    }

    private void verifyProductId(String accountProductId) {
        if (Strings.isNullOrEmpty(accountProductId)) {
            throw new IllegalStateException("Missing accountproductID!");
        }
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {
        try {
            String accountProductId =
                    account.getFromTemporaryStorage(FortisConstants.Storage.ACCOUNT_PRODUCT_ID);

            verifyProductId(accountProductId);

            return apiClient.fetchTransactions(page, accountProductId);
        } catch (HttpResponseException hre) {
            return PaginatorResponseImpl.createEmpty(false);
        }
    }

    private boolean onlySavings(TransactionalAccount account) {
        return AccountTypes.SAVINGS == account.getType();
    }
}
