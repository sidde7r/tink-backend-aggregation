package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.rpc.TransactionalDetailsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaConstants.Storage.TICKET;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaConstants.Storage.USERNAME;

public class IberCajaTransactionalFetcher implements TransactionDatePaginator<TransactionalAccount> {

    private final IberCajaApiClient bankClient;
    private final SessionStorage storage;

    public IberCajaTransactionalFetcher(IberCajaApiClient bankClient,
            SessionStorage storage) {
        this.bankClient = bankClient;
        this.storage = storage;
    }

    private static String format(Date date) {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(date);
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, Date fromDate, Date toDate) {

        TransactionalDetailsResponse tranactionalDetailsRespone = bankClient
                .fetchTransactionDetails(account.getBankIdentifier(), storage.get(TICKET), format(fromDate),
                        format(toDate), storage.get(USERNAME));
        return tranactionalDetailsRespone;
    }
}
