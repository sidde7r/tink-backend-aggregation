package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.creditcard;

import com.google.common.base.Strings;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.creditcard.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.creditcard.entities.CardTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.creditcard.rpc.CardTransactionsResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.libraries.serialization.utils.SerializationUtils;


public class BankiaCreditCardFetcher implements AccountFetcher<CreditCardAccount>,
        TransactionFetcher<CreditCardAccount> {

    private static final AggregationLogger LOGGER = new AggregationLogger(BankiaCreditCardFetcher.class);
    private final BankiaApiClient apiClient;

    public BankiaCreditCardFetcher(BankiaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
//        return apiClient.getCards().stream()
//                .map(cardEntity -> cardEntity.toTinkAccount())
//                .collect(Collectors.toList());

        List<CardEntity> cards = apiClient.getCards();
        cards.forEach(this::logNonDebitCards);

        return Collections.emptyList();
    }

    /**
     * Log any card that does not have card type set to "D". Theory is that credit cards will be of
     * type "C". But since we're not sure we have to wait for logs.
     */
    private void logNonDebitCards(CardEntity cardEntity) {
        String cardType = cardEntity.getContract().getCardType();

        if (Strings.isNullOrEmpty(cardType) ||
                !cardType.equalsIgnoreCase(BankiaConstants.CardTypes.DEBIT_CARD)) {

            LOGGER.infoExtraLong(SerializationUtils.serializeToString(cardEntity),
                    BankiaConstants.Logging.NON_DEBIT_CARD);
        }
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(CreditCardAccount account) {

        // The variable `hasMore` should indicate whether there are more transactions to fetch,
        // but we don't know how the pagination works. For account transactions there are several
        // criteria one can set in the request, but these cause a 500 response when applied to
        // fetching of card transactions.
        //
        //  *   hasMore showed false when there were two existing transactions, but only one was requested.
        //  *   repeatedly querying the endpoint gives the same transactions over and over again
        //  *   fetching up to 999 is allowed, more transactions results in a 500 response
        //  *   providing 20 (app default) transactions in the response and setting hasMore to true
        //      does not make the app request more transactions

        String id = account.getBankIdentifier();

        int maxLimit = 100;
        CardTransactionsResponse response = apiClient.getCardTransactions(id, maxLimit);
        boolean hasMore = response.isIndicateMoreTransactions();
        List<AggregationTransaction> transactions = response.getTransactions()
                .stream()
                .map(CardTransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());

        if (hasMore) {
            LOGGER.warn(String.format("Fetched %d transactions when limit was set to %d. hasMore was set to %s.",
                    transactions.size(), maxLimit, Boolean.toString(hasMore)));
        }

        return transactions;
    }
}
