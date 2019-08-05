package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bpost.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bpost.BpostConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.Transactions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.Xs2aDevelopersTransactionalAccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class BpostTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionDatePaginator<TransactionalAccount> {

    private final Xs2aDevelopersTransactionalAccountFetcher
            xs2aDevelopersTransactionalAccountFetcher;
    private final Xs2aDevelopersApiClient apiClient;
    private int counter;

    public BpostTransactionalAccountFetcher(Xs2aDevelopersApiClient apiClient) {
        this.apiClient = apiClient;
        this.xs2aDevelopersTransactionalAccountFetcher =
                new Xs2aDevelopersTransactionalAccountFetcher(apiClient);
        this.counter = 0;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return xs2aDevelopersTransactionalAccountFetcher.fetchAccounts();
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        try {
            return PaginatorResponseImpl.create(
                    apiClient.getTransactions(account, fromDate, toDate).toTinkTransactions());
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == Transactions.ERROR_CODE_MAX_ACCESS_EXCEEDED) {
                return PaginatorResponseImpl.createEmpty(false);
            } else if (e.getResponse().getStatus() == Transactions.ERROR_CODE_INTERNAL_SERVER) {
                ++counter;
                if (counter < BpostConstants.TRANSACTION_FETCHING_TIME_LIMIT) {
                    return PaginatorResponseImpl.createEmpty(true);
                } else {
                    return PaginatorResponseImpl.createEmpty(false);
                }
            } else {
                throw e;
            }
        }
    }
}
