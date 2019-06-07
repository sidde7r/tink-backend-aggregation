package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class IngBaseTransactionsFetcher implements TransactionDatePaginator<TransactionalAccount> {

    private final IngBaseApiClient apiClient;

    public IngBaseTransactionsFetcher(IngBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {

        // TODO - Temporary setup Sandbox specification
        Date from = new Date(), to = new Date();
        try {
            from = new SimpleDateFormat("yyyy/MM/dd").parse("2016/10/01");
            to = new SimpleDateFormat("yyyy/MM/dd").parse("2016/11/21");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return apiClient
                .fetchTransactions(
                        account.getFromTemporaryStorage(StorageKeys.TRANSACTIONS_URL), from, to)
                .setFetchNextFunction(apiClient::fetchTransactions);
    }
}
