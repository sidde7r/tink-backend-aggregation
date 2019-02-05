package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.creditcard;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.creditcard.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.agents.rpc.Credentials;

public class SamlinkCreditCardFetcher implements AccountFetcher<CreditCardAccount>,
        TransactionKeyPaginator<CreditCardAccount, SamlinkCreditCardFetcher.TransactionKey> {

    private static final AggregationLogger log = new AggregationLogger(SamlinkCreditCardFetcher.class);

    private final SamlinkApiClient apiClient;

    public SamlinkCreditCardFetcher(SamlinkApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return apiClient.getCreditCards().toCreditCardAccounts(
                apiClient::getCardDetails, message ->
                        log.infoExtraLong(message, SamlinkConstants.LogTags.CREDITCARD));
    }

    @Override
    public TransactionKeyPaginatorResponse<TransactionKey> getTransactionsFor(
            CreditCardAccount account, TransactionKey key) {
        if (key == null) {
            return fetchInitialTransactions(account);
        }
        return fetchNextTransactions(account, key);
    }

    private TransactionKeyPaginatorResponseImpl<TransactionKey> fetchInitialTransactions(CreditCardAccount account) {
        return apiClient.getTransactions(account)
                .map(creditCardTransactions ->
                        createResponse(account, creditCardTransactions, new TransactionKey(creditCardTransactions))
                )
                .orElseGet(TransactionKeyPaginatorResponseImpl::new);
    }

    private TransactionKeyPaginatorResponseImpl<TransactionKey> fetchNextTransactions(
            CreditCardAccount account, TransactionKey key) {
        return apiClient.getTransactions(key.creditCardTransactions, key.offset)
                .map(creditCardTransactions ->
                        createResponse(account, creditCardTransactions, new TransactionKey(creditCardTransactions, key))
                ).orElseGet(TransactionKeyPaginatorResponseImpl::new);
    }

    private TransactionKeyPaginatorResponseImpl<TransactionKey> createResponse(
            CreditCardAccount account, CreditCardTransactionsResponse creditCardTransactions,
            TransactionKey key) {
        TransactionKeyPaginatorResponseImpl<TransactionKey> response =
                new TransactionKeyPaginatorResponseImpl<>();
        response.setTransactions(creditCardTransactions.toTinkTransactions(account));
        response.setNext(key);
        return response;
    }

    class TransactionKey {
        private final CreditCardTransactionsResponse creditCardTransactions;
        private final int offset;

        TransactionKey(CreditCardTransactionsResponse creditCardTransactions) {
            this.creditCardTransactions = creditCardTransactions;
            this.offset = creditCardTransactions.size();
        }

        TransactionKey(CreditCardTransactionsResponse creditCardTransactions, TransactionKey previousKey) {
            this.creditCardTransactions = creditCardTransactions;
            this.offset = previousKey.offset + creditCardTransactions.size();
        }
    }
}
