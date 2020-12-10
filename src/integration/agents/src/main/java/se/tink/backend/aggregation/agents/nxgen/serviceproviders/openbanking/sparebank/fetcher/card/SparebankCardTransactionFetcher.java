package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card;

import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card.rpc.CardTransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.mapper.SparebankCardTransactionMapper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

@RequiredArgsConstructor
public class SparebankCardTransactionFetcher
        implements TransactionKeyPaginator<CreditCardAccount, String> {

    private final SparebankApiClient apiClient;
    private final SparebankCardTransactionMapper cardTransactionMapper;

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            CreditCardAccount account, @Nullable String key) {

        CardTransactionResponse cardTransactionResponse;
        if (key == null) {
            cardTransactionResponse = apiClient.fetchCardTransactions(account.getApiIdentifier());
        } else {
            cardTransactionResponse = apiClient.fetchNextCardTransactions(key);
        }

        return new TransactionKeyPaginatorResponseImpl<>(
                cardTransactionMapper.toTinkTransactions(
                        cardTransactionResponse.getCardTransactions()),
                cardTransactionResponse.getNext().orElse(null));
    }
}
