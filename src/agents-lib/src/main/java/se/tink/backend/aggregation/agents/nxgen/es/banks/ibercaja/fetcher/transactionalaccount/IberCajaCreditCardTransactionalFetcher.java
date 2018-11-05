package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.rpc.CreditCardResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaConstants.Storage.TICKET;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaConstants.Storage.USERNAME;

public class IberCajaCreditCardTransactionalFetcher implements TransactionDatePaginator<CreditCardAccount> {

    private final IberCajaApiClient bankClient;
    private final SessionStorage storage;

    public IberCajaCreditCardTransactionalFetcher(IberCajaApiClient bankClient,
            SessionStorage storage) {
        this.bankClient = bankClient;
        this.storage = storage;
    }

    private static String format(Date date) {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(date);
    }

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, Date fromDate, Date toDate) {
        CreditCardResponse creditCardResponse = bankClient.fetchCreditCardsTransactionList(account.getBankIdentifier(),
                IberCajaConstants.DefaultRequestParams.REQUEST_ORDER,
                IberCajaConstants.DefaultRequestParams.REQUEST_TYPE, storage.get(TICKET), storage.get(USERNAME),
                format(fromDate), format(toDate));
        return creditCardResponse;
    }
}
