package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaConstants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class IberCajaCreditCardTransactionalFetcher implements TransactionDatePaginator<CreditCardAccount> {

    private final IberCajaApiClient bankClient;

    public IberCajaCreditCardTransactionalFetcher(IberCajaApiClient bankClient) {
        this.bankClient = bankClient;
    }

    private static String format(Date date) {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(date);
    }

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, Date fromDate, Date toDate) {

        return bankClient.fetchCreditCardsTransactionList(
                account.getBankIdentifier(),
                IberCajaConstants.DefaultRequestParams.REQUEST_ORDER,
                IberCajaConstants.DefaultRequestParams.REQUEST_TYPE,
                format(fromDate), format(toDate));
    }
}
