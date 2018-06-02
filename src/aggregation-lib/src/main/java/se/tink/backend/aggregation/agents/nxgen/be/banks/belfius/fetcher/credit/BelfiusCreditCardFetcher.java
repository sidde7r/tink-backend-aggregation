package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.credit;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.BelfiusTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.entities.BelfiusProduct;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.entities.BelfiusTransaction;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class BelfiusCreditCardFetcher implements AccountFetcher<CreditCardAccount>,TransactionDatePaginator<CreditCardAccount> {

    private static final AggregationLogger LOGGER = new AggregationLogger(BelfiusCreditCardFetcher.class);

    private final BelfiusApiClient apiClient;

    public BelfiusCreditCardFetcher(BelfiusApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        // When implementing credit cards for real we need to make sure that we handle empty strings as values.
        // This has been logged (user specific values anonymized):
        // {"orderingAccount":"N","typeAccount":"F0","denominationCode":"XXXX","numberAccount":"1111 1111 1111",
        // "denominationDescription":"MASTERCARD","extIntAccount":"111111111111","amount":"",
        // "effectiveAvailableCard":"","available":"","creditcardActionAllowed":"LIMITS","holderName":NAME,
        // "nbRecyclage":"","totalClosureCard":"","zoomItAllowed":"N"}

        // This leads to a NPE in UpdateController.updateAccount(UpdateController.java:46), so I don't think we're
        // handling the empty strings correctly. Currently only logging since we were not suppose to implement
        // credit cards right now to begin with.

        try {
            this.apiClient.fetchProducts().stream()
                    .filter(entry -> entry.getValue().isCreditCard())
                    .forEach(entry -> {
                                BelfiusProduct product = entry.getValue();
                                LOGGER.infoExtraLong("card: " + product, BelfiusConstants.Fetcher.CreditCards.LOGTAG);
                                // return product.toCreditCardAccount(entry.getKey());
                            }
                    );
        } catch (Exception e) {
            LOGGER.warnExtraLong("Unable to fetch credit cards", BelfiusConstants.Fetcher.CreditCards.LOGTAG, e);
        }

        return Collections.emptyList();
    }

    /**
     * This implementation is copied from
     * {@link BelfiusTransactionalAccountFetcher#getTransactionsFor(TransactionalAccount, Date, Date)}
     *  to be able to log. If no change is necessary a refactoring could be done to combine these classes.
     */
    @Override
    public Collection<? extends Transaction> getTransactionsFor(CreditCardAccount account, Date fromDate, Date toDate) {
        try {
            String key = account.getBankIdentifier();
            List<BelfiusTransaction> transactions = this.apiClient.fetchTransactions(key, fromDate, toDate)
                    .stream().collect(Collectors.toList());
            LOGGER.infoExtraLong("transactions: " + transactions, BelfiusConstants.Fetcher.CreditCards.LOGTAG);

            // return transactions.stream()
            //        .map(BelfiusTransaction::toTinkTransaction)
            //        .filter(Objects::nonNull)
            //        .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.warnExtraLong("Unable to fetch credit card transactions",
                    BelfiusConstants.Fetcher.CreditCards.LOGTAG, e);
        }
        
        return Collections.emptyList();
    }
}
