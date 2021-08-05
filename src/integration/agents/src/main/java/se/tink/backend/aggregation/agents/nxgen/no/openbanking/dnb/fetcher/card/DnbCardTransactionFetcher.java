package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.card;

import java.util.Collection;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbStorage;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.rpc.CardTransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.mapper.DnbTransactionMapper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.credentials.service.UserAvailability;

@Slf4j
@AllArgsConstructor
public class DnbCardTransactionFetcher implements TransactionDatePaginator<CreditCardAccount> {

    private final DnbStorage storage;
    private final DnbApiClient apiClient;
    private final DnbTransactionMapper transactionMapper;
    private final UserAvailability userAvailability;

    @Override
    public PaginatorResponse getTransactionsFor(
            CreditCardAccount account, Date fromDate, Date toDate) {
        CardTransactionResponse cardTransactionResponse =
                apiClient.fetchCardTransactions(
                        storage.getConsentId(), account.getApiIdentifier(), fromDate, toDate);
        Collection<Transaction> tinkTransactions =
                transactionMapper.toTinkTransactions(cardTransactionResponse.getCardTransactions());

        log.info("[DNB OB] Fetched {} credit card transactions", tinkTransactions.size());

        // Allow to fetch more only in case of manual refresh (user present), to not exhaust 4-a-day
        // limit with just card transactions
        return PaginatorResponseImpl.create(tinkTransactions, userAvailability.isUserPresent());
    }
}
