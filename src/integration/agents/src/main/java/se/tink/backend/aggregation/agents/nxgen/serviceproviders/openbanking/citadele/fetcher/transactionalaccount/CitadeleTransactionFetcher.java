package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount.entity.transaction.TransactionsBaseResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class CitadeleTransactionFetcher implements TransactionDatePaginator<TransactionalAccount> {

    private final CitadeleBaseApiClient apiClient;
    private final String providerMarket;

    public CitadeleTransactionFetcher(CitadeleBaseApiClient apiClient, String providerMarket) {
        this.apiClient = apiClient;
        this.providerMarket = providerMarket;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        TransactionsBaseResponse baseResponse =
                apiClient.getTransactions(
                        account.getApiIdentifier(), toLocalDate(fromDate), toLocalDate(toDate));
        List<Transaction> transactions =
                new ArrayList<>(getTinkTransactions(providerMarket, baseResponse));

        return PaginatorResponseImpl.create(transactions, !transactions.isEmpty());
    }

    public List<Transaction> getTinkTransactions(
            String providerMarket, TransactionsBaseResponse baseResponse) {
        List<Transaction> booked =
                baseResponse.getTransactions().getBooked().stream()
                        .map(
                                transactionBaseEntity ->
                                        transactionBaseEntity.toTinkTransaction(
                                                providerMarket, false))
                        .collect(Collectors.toList());

        List<Transaction> result = new ArrayList<>(booked);

        if (baseResponse.getTransactions().getPending() != null) {
            List<Transaction> pending =
                    baseResponse.getTransactions().getPending().stream()
                            .map(
                                    transactionBaseEntity ->
                                            transactionBaseEntity.toTinkTransaction(
                                                    providerMarket, true))
                            .collect(Collectors.toList());
            result.addAll(pending);
        }
        return result;
    }

    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
