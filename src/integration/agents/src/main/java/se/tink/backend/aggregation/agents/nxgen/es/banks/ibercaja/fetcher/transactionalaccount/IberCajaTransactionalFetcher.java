package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class IberCajaTransactionalFetcher
        implements TransactionDatePaginator<TransactionalAccount> {

    private final IberCajaApiClient bankClient;

    public IberCajaTransactionalFetcher(IberCajaApiClient bankClient) {
        this.bankClient = bankClient;
    }

    private static String format(Date date) {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(date);
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {

        return bankClient.fetchTransactionDetails(
                account.getBankIdentifier(), format(fromDate), format(toDate));
    }
}
