package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants.Storage.TIME_LINES;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants.Storage.TRANSACTIONS;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Configuration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TimelineResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TransactionResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class AmericanExpressV62TransactionFetcher
        implements TransactionPagePaginator<CreditCardAccount> {
    private final AmericanExpressV62Configuration config;
    private final SessionStorage sessionStorage;

    private AmericanExpressV62TransactionFetcher(
            AmericanExpressV62Configuration config, SessionStorage sessionStorage) {
        this.config = config;
        this.sessionStorage = sessionStorage;
    }

    public static AmericanExpressV62TransactionFetcher create(
            AmericanExpressV62Configuration config, SessionStorage sessionStorage) {
        return new AmericanExpressV62TransactionFetcher(config, sessionStorage);
    }

    @Override
    /**
     * Fetches the transactions from two parts of the Amex api, the actual transactions and the
     * 'timeline'. The 'timeline' is some sort of overview, where pending transactions end up. We
     * parse the timeline to fetch them.
     */
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, int page) {
        Set<Transaction> transactions = new HashSet<>();
        // Fetch transactions
        Set<TransactionResponse> transactionResponses =
                sessionStorage
                        .get(TRANSACTIONS, new TypeReference<Set<TransactionResponse>>() {})
                        .orElse(Collections.emptySet());
        for (TransactionResponse response : transactionResponses) {
            if (!response.isOkResponse()) {
                continue;
            }
            final String suppIndex = response.getSuppIndexForAccount(account);
            transactions.addAll(response.toTinkTransactions(config, false, suppIndex));
        }

        // Fetch timeline and parse pending transactions from it.
        Set<TimelineResponse> timelineResponses =
                sessionStorage
                        .get(TIME_LINES, new TypeReference<Set<TimelineResponse>>() {})
                        .orElse(Collections.emptySet());
        for (TimelineResponse timelineResponse : timelineResponses) {
            final String suppIndex = timelineResponse.getSuppIndexForAccount(account);
            transactions.addAll(timelineResponse.getPendingTransactions(config, suppIndex));
        }
        return PaginatorResponseImpl.create(transactions, false);
    }
}
