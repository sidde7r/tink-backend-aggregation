package se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.seb.fetcher.creditcards;

import java.time.Month;
import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.seb.utils.SebStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.entities.TransactionsEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionMonthPaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

public class SebCreditCardTransactionsFetcher
        implements TransactionMonthPaginator<CreditCardAccount> {

    private final SebStorage instanceStorage;
    private final String providerMarket;

    public SebCreditCardTransactionsFetcher(SebStorage instanceStorage, String providerMarket) {
        this.instanceStorage = instanceStorage;
        this.providerMarket = providerMarket;
    }

    /**
     * In this method, instead of fetching the transactions from the Bank's aPI, we get them from
     * the instanceStorage (we stored all the {@link TransactionsEntity} responses while creating
     * the CreditCardAccounts in {@link SebCreditCardAccountFetcher})
     *
     * <p>Each of the {@link TransactionsEntity} is streamed over and {@link
     * CreditCardTransaction}(s) that belog to the particular {@link CreditCardAccount} are filtered
     * based on the accountNumber.
     *
     * @param account - Account for which {@link CreditCardTransaction}(s) are needed to be fetched.
     * @param year - not used
     * @param month - not used
     * @return List<CreditCardTransaction> wrapped in {@link PaginatorResponse}. Since we have
     *     already fetched all the available transactions for the user, so we set 'canFetchMore' to
     *     false in first go only.
     */
    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, Year year, Month month) {
        List<TransactionsEntity> transactions =
                instanceStorage.getAllCreditCardTransactionEntities();
        List<CreditCardTransaction> creditCardTransactions =
                transactions.stream()
                        .map(
                                transactionEntity ->
                                        transactionEntity.toTinkTransactions(
                                                account.getAccountNumber(), providerMarket))
                        .flatMap(List::stream)
                        .collect(Collectors.toList());
        return PaginatorResponseImpl.create(creditCardTransactions, false);
    }
}
