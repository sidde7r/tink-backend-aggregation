package se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.SantanderApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class SantanderInvestmentTransactionFetcher
        implements TransactionPagePaginator<InvestmentAccount> {

    private static final int PAGE_SIZE = 1000;

    private final SantanderApiClient apiClient;

    public SantanderInvestmentTransactionFetcher(SantanderApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(InvestmentAccount account, int page) {
        List<Map<String, String>> businessData =
                apiClient
                        .fetchInvestmentTransactions(account.getAccountNumber(), page, PAGE_SIZE)
                        .getBusinessData();

        return businessData.stream()
                .map(this::toTinkTransaction)
                .collect(
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                transactions ->
                                        PaginatorResponseImpl.create(
                                                transactions, !transactions.isEmpty())));
    }

    private Transaction toTinkTransaction(Map<String, String> transaction) {
        DateTimeFormatter transactionDatePattern = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        return Transaction.builder()
                .setDescription(transaction.get(Fields.Transaction.DESCRIPTION))
                .setDate(
                        LocalDate.parse(
                                transaction.get(Fields.Transaction.OPERATION_DATE),
                                transactionDatePattern))
                .setAmount(
                        ExactCurrencyAmount.of(
                                calculateTransactionAmount(
                                        new BigDecimal(
                                                transaction.get(
                                                        Fields.Transaction.NUMBER_OF_UNITS)),
                                        new BigDecimal(transaction.get(Fields.Transaction.AMOUNT))),
                                transaction.get(Fields.Transaction.CURRENCY)))
                .build();
    }

    private BigDecimal calculateTransactionAmount(BigDecimal units, BigDecimal operationAmount) {
        if (units.signum() == -1) {
            return operationAmount.negate();
        } else {
            return operationAmount;
        }
    }
}
