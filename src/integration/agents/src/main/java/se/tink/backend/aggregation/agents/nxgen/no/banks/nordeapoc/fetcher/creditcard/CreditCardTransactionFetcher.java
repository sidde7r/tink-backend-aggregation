package se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.creditcard;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.client.FetcherClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.creditcard.entity.CreditCardTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.creditcard.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@AllArgsConstructor
@Slf4j
public class CreditCardTransactionFetcher implements TransactionPagePaginator<CreditCardAccount> {

    private static final int PAGE_SIZE = 100;
    private FetcherClient fetcherClient;

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, int page) {
        try {
            CreditCardTransactionsResponse creditCardTransactionsResponse =
                    fetcherClient.fetchCreditCardTransactions(account.getApiIdentifier(), page);
            List<Transaction> tinkCreditCardTransactions =
                    creditCardTransactionsResponse.getTransactions().stream()
                            .map(this::toTinkTransaction)
                            .collect(Collectors.toList());
            return PaginatorResponseImpl.create(
                    tinkCreditCardTransactions, tinkCreditCardTransactions.size() == PAGE_SIZE);
        } catch (HttpResponseException e) {
            return PaginatorResponseImpl.createEmpty(false);
        }
    }

    private Transaction toTinkTransaction(CreditCardTransactionEntity cardTransactionEntity) {
        return Transaction.builder()
                .setType(TransactionTypes.CREDIT_CARD)
                .setDate(
                        ObjectUtils.firstNonNull(
                                cardTransactionEntity.getBookingDate(),
                                cardTransactionEntity.getTransactionDate()))
                .setPending(!cardTransactionEntity.isBooked())
                .setAmount(cardTransactionEntity.getAmount())
                .setDescription(cardTransactionEntity.getTitle())
                .setPayload(
                        TransactionPayloadTypes.EXTERNAL_ID,
                        cardTransactionEntity.getTransactionId())
                .build();
    }
}
