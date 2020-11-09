package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.card;

import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbStorage;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.rpc.CardTransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.mapper.DnbTransactionMapper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

@AllArgsConstructor
public class DnbCardTransactionFetcher
        implements TransactionKeyPaginator<CreditCardAccount, String> {

    private final DnbStorage storage;
    private final DnbApiClient apiClient;
    private final DnbTransactionMapper transactionMapper;

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            CreditCardAccount account, @Nullable String key) {

        CardTransactionResponse cardTransactionResponse =
                apiClient.fetchCardTransactions(storage.getConsentId(), account.getApiIdentifier());

        // DNB currently does not support card transaction pagination
        return new TransactionKeyPaginatorResponseImpl<>(
                transactionMapper.toTinkTransactions(cardTransactionResponse.getCardTransactions()),
                null);
    }
}
