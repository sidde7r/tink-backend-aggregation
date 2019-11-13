package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.creditcard;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.MontepioApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.MontepioConstants;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.creditcard.entity.CreditCardEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.creditcard.entity.CreditCardTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class MontepioCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionPagePaginator<CreditCardAccount> {

    private final MontepioApiClient apiClient;
    private static final String CREDIT_CARD_LIMIT_DETAILS_KEY = "Limite de crédito";

    public MontepioCreditCardFetcher(final MontepioApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient);
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return apiClient.fetchCreditCards().getResult().getCreditCards().stream()
                .map(this::mapAccount)
                .collect(Collectors.toList());
    }

    private CreditCardAccount mapAccount(CreditCardEntity accountEntity) {

        String creditLimit =
                apiClient.fetchCreditCardDetails(accountEntity.getHandle()).getResult()
                        .getAccountDetails().stream()
                        .filter(a -> CREDIT_CARD_LIMIT_DETAILS_KEY.equals(a.getKey()))
                        .findAny()
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Did not find credit limit for card in card details"))
                        .getValue();
        return accountEntity.toTinkAccount(Double.valueOf(creditLimit));
    }

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, int page) {
        LocalDate to = LocalDate.now();
        LocalDate from =
                LocalDate.now().minusMonths(MontepioConstants.MAX_TRANSACTION_HISTORY_MONTHS);

        FetchTransactionsResponse response =
                apiClient.fetchCreditCardTransactions(account, page, from, to);

        response.getError()
                .ifPresent(
                        errorEntity -> {
                            throw new IllegalStateException(
                                    String.format(
                                            MontepioConstants.TRANSACTIONS_FETCH_ERROR_FORMAT,
                                            errorEntity.getCode(),
                                            errorEntity.getMessage()));
                        });

        List<Transaction> transactions =
                response.getResultEntity().getCardTransactions().orElseGet(Collections::emptyList)
                        .stream()
                        .map(CreditCardTransactionEntity::toTinkTransaction)
                        .collect(Collectors.toList());
        return PaginatorResponseImpl.create(
                transactions, response.getResultEntity().hasMorePages());
    }
}
