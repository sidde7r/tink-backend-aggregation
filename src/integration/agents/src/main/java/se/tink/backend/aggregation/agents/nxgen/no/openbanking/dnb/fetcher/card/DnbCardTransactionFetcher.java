package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.card;

import java.util.Collection;
import java.util.Date;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbStorage;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.rpc.CardTransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.mapper.DnbTransactionMapper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@AllArgsConstructor
public class DnbCardTransactionFetcher implements TransactionDatePaginator<CreditCardAccount> {

    private final DnbStorage storage;
    private final DnbApiClient apiClient;
    private final DnbTransactionMapper transactionMapper;

    @Override
    public PaginatorResponse getTransactionsFor(
            CreditCardAccount account, Date fromDate, Date toDate) {
        CardTransactionResponse cardTransactionResponse =
                apiClient.fetchCardTransactions(
                        storage.getConsentId(), account.getApiIdentifier(), fromDate, toDate);
        Collection<Transaction> tinkTransactions =
                transactionMapper.toTinkTransactions(cardTransactionResponse.getCardTransactions());

        return PaginatorResponseImpl.create(tinkTransactions, true);
    }
}
