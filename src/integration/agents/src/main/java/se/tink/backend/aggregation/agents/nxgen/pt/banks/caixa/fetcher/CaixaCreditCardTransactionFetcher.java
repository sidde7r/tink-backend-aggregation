package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.CaixaApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities.CardTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.rpc.CardAccountDetailsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class CaixaCreditCardTransactionFetcher
        implements TransactionPagePaginator<CreditCardAccount> {

    private static final int MAX_TRANSACTION_FETCH_MONTHS = 24;
    private static final int VALUE_DECIMAL_PLACES = 2;
    private final CaixaApiClient apiClient;

    public CaixaCreditCardTransactionFetcher(CaixaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, int page) {
        YearMonth statementDate = YearMonth.now().minusMonths(page);

        CardAccountDetailsResponse cardAccountDetailsResponse =
                apiClient.fetchCardAccountTransactions(account.getApiIdentifier(), statementDate);
        CardEntity card = cardAccountDetailsResponse.getCards().get(0);

        List<CardTransactionEntity> transactions =
                YearMonth.now().equals(statementDate)
                        ? cardAccountDetailsResponse.getCardAccountTransactions().getTransactions()
                        : card.getTransactions();

        List<Transaction> mappedTransactions =
                Optional.ofNullable(transactions).orElse(Collections.emptyList()).stream()
                        .map(
                                transaction ->
                                        toTinkTransaction(
                                                transaction, card.getCardAccountCurrency()))
                        .collect(Collectors.toList());

        boolean canFetchMore = !mappedTransactions.isEmpty() || page < MAX_TRANSACTION_FETCH_MONTHS;
        return PaginatorResponseImpl.create(mappedTransactions, canFetchMore);
    }

    private Transaction toTinkTransaction(CardTransactionEntity transaction, String currency) {
        BigDecimal amount =
                transaction
                        .getCreditAmount()
                        .subtract(transaction.getDebitAmount())
                        .movePointLeft(VALUE_DECIMAL_PLACES);

        return Transaction.builder()
                .setType(TransactionTypes.CREDIT_CARD)
                .setAmount(ExactCurrencyAmount.of(amount, currency))
                .setDate(transaction.getBookDate())
                .setDescription(transaction.getDescription())
                .setRawDetails(transaction.getCardDescription())
                .build();
    }
}
