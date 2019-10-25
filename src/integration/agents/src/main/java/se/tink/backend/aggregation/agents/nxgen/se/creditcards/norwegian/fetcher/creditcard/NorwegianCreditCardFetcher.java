package se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.fetcher.creditcard;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.NorwegianApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.NorwegianConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.fetcher.common.TransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.fetcher.common.entity.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.fetcher.creditcard.entity.CreditCardEntity;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.fetcher.creditcard.entity.CreditCardResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class NorwegianCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionDatePaginator<CreditCardAccount> {

    private final NorwegianApiClient apiClient;
    private String accountNo;

    public NorwegianCreditCardFetcher(NorwegianApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        // Fetch balance and credit
        final CreditCardResponse creditCardResponse = apiClient.fetchCardBalance();

        if (creditCardResponse == null) {
            return Lists.newArrayList();
        }

        // Fetch card number
        final CreditCardEntity cardEntity = apiClient.fetchCardList();

        return Optional.ofNullable(cardEntity)
                .map(creditCardResponse::toTinkAccount)
                .map(Lists::newArrayList)
                .orElseGet(Lists::newArrayList);
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            CreditCardAccount account, Date fromDate, Date toDate) {
        return TransactionFetcher.fetchTransactionsFor(apiClient, getAccountNo(), fromDate, toDate);
    }

    private String getAccountNo() {
        if (accountNo == null) {
            accountNo = apiClient.fetchCreditCardAccountNumber();
        }

        return accountNo;
    }

    public PaginatorResponse fetchUninvoicedTransactions() {
        List<Transaction> transactions =
                apiClient.fetchTransactions(getAccountNo(), "", "", QueryValues.GET_LAST_DAYS_TRUE)
                        .stream()
                        .map(TransactionEntity::toTinkTransaction)
                        .collect(Collectors.toList());

        return PaginatorResponseImpl.create(transactions, true);
    }
}
