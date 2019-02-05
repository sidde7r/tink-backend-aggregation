package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.creditcard;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.NordeaV20ApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.NordeaV20Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.creditcard.rpc.CardDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.creditcard.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.parsers.NordeaV20Parser;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

public class NordeaV20CreditCardFetcher implements AccountFetcher<CreditCardAccount>,
        TransactionKeyPaginator<CreditCardAccount, String> {

    private static final AggregationLogger log = new AggregationLogger(NordeaV20CreditCardFetcher.class);
    private final NordeaV20ApiClient client;
    private final NordeaV20Parser parser;

    public NordeaV20CreditCardFetcher(NordeaV20ApiClient client, NordeaV20Parser parser) {
        this.client = client;
        this.parser = parser;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {

        try {
            return client.getAccountProductsOfTypes(NordeaV20Constants.ProductType.CARD).stream()
                    .filter(pe -> !NordeaV20Constants.CardGroup.DEBIT_CARD.equalsIgnoreCase(pe.getCardGroup()))
                    .map(pe -> {
                        CardDetailsResponse cardDetailsResponse = client.fetchCardDetails(pe.getNordeaAccountIdV2());
                        return parser.parseCreditCardAccount(pe, cardDetailsResponse.getCardDetails());
                    }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Could not fetch credit card: ", e);
            return Collections.emptyList();
        }
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(final CreditCardAccount account, String key) {
        if (!client.canViewTransactions(account)) {
            return new TransactionKeyPaginatorResponseImpl<>();
        }

        CreditCardTransactionsResponse transactionsResponse = client.fetchCreditCardTransactions(
                account.getBankIdentifier(), key);

        Collection<CreditCardTransaction> transactions = transactionsResponse.getTransactions().stream()
                .map(te -> {
                    CreditCardTransaction.Builder builder = parser.parseTransaction(te);
                    builder.setCreditAccount(account);
                    return builder.build();
                }).collect(Collectors.toList());

        TransactionKeyPaginatorResponseImpl<String> response = new TransactionKeyPaginatorResponseImpl<>();
        response.setTransactions(transactions);
        response.setNext(transactionsResponse.getContinuationKey());

        return response;
    }
}
