package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher;

import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.CaixaApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities.CardTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.rpc.CardAccountTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class CaixaCreditCardTransactionFetcher
        implements TransactionPagePaginator<CreditCardAccount> {

    private static final int MAX_TRANSACTION_FETCH_MONTHS = 24;

    private final CaixaApiClient apiClient;

    public CaixaCreditCardTransactionFetcher(CaixaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, int page) {
        YearMonth statementDate = YearMonth.now().minusMonths(page);

        CardAccountTransactionsResponse transactionsResponse =
                apiClient.fetchCardAccountTransactions(account.getApiIdentifier(), statementDate);
        CardEntity card = transactionsResponse.getCards().get(0);

        List<CardTransactionEntity> transactions =
                YearMonth.now().equals(statementDate)
                        ? transactionsResponse.getCardAccountTransactions().getTransactions()
                        : card.getTransactions();

        List<Transaction> mappedTransactions =
                Optional.ofNullable(transactions).orElse(Collections.emptyList()).stream()
                        .map(tran -> tran.toTinkTransaction(card.getCardAccountCurrency()))
                        .collect(Collectors.toList());

        boolean canFetchMore = !mappedTransactions.isEmpty() || page < MAX_TRANSACTION_FETCH_MONTHS;
        return PaginatorResponseImpl.create(mappedTransactions, canFetchMore);
    }
}
