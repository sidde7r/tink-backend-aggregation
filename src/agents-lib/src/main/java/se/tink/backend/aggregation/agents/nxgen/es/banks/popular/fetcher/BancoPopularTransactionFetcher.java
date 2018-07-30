package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.BancoPopularApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.BancoPopularPersistenStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.entities.BancoPopularContract;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.rpc.FetchTransactionsRequest;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class BancoPopularTransactionFetcher implements TransactionDatePaginator<TransactionalAccount> {

    private final BancoPopularApiClient bankClient;
    private final BancoPopularPersistenStorage persistentStorage;

    public BancoPopularTransactionFetcher(BancoPopularApiClient bankClient,
            BancoPopularPersistenStorage persistentStorage) {

        this.bankClient = bankClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, Date fromDate, Date toDate) {
        BancoPopularContract contract = persistentStorage.getLoginContracts().getFirstContract();

        FetchTransactionsRequest fetchTransactionsRequest = new FetchTransactionsRequest()
                .setCccBanco(intToZeroFilledString(contract.getBanco()))
                .setNumIntContrato(account.getBankIdentifier())
                .setCccSucursal(intToZeroFilledString(contract.getOficina()))
                .setFechaDesde(formatDate(fromDate))
                .setFechaHasta(formatDate(toDate))
                .updateCccFields(account.getAccountNumber());

        Collection<Transaction> transactions = bankClient.fetchTransactions(fetchTransactionsRequest)
                .getTinkTransactions();

        // NOTE/Todo: The response contain a field called `hayMas` which means `there is more`. Which should be used
        // as `canFetchMore()`.
        return PaginatorResponseImpl.create(transactions);
    }

    private String intToZeroFilledString(int i) {
        return String.format("%04d", i);
    }

    private String formatDate(Date aDate) {
        LocalDate date = new java.sql.Date(aDate.getTime()).toLocalDate();
        return date.format(DateTimeFormatter.ISO_DATE);
    }
}
