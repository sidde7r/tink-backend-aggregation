package se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.SantanderApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class SantanderInvestmentTransactionFetcher
        implements TransactionFetcher<InvestmentAccount> {

    private static final int PAGE_SIZE = 1000;
    private final SantanderApiClient apiClient;

    public SantanderInvestmentTransactionFetcher(SantanderApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(InvestmentAccount account) {

        List<AggregationTransaction> allTransactions = new ArrayList<>();
        List<Transaction> transactionPage;
        int currentPage = 1;
        do {
            ApiResponse<Map<String, String>> apiResponse =
                    apiClient.fetchInvestmentTransactions(
                            account.getAccountNumber(), currentPage, PAGE_SIZE);

            transactionPage =
                    apiResponse.getBusinessData().stream()
                            .map(this::toTinkTransaction)
                            .collect(
                                    Collectors.collectingAndThen(
                                            Collectors.toList(), Collections::unmodifiableList));

            allTransactions.addAll(transactionPage);
            currentPage++;
        } while (!transactionPage.isEmpty());

        return allTransactions;
    }

    private Transaction toTinkTransaction(Map<String, String> obj) {
        DateTimeFormatter transactionDatePattern = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        return Transaction.builder()
                .setDescription(obj.get(Fields.Transaction.DESCRIPTION))
                .setDate(
                        LocalDate.parse(
                                obj.get(Fields.Transaction.OPERATION_DATE), transactionDatePattern))
                .setAmount(
                        ExactCurrencyAmount.of(
                                calculateTransactionAmount(
                                        new BigDecimal(obj.get(Fields.Transaction.NUMBER_OF_UNITS)),
                                        new BigDecimal(obj.get(Fields.Transaction.AMOUNT))),
                                obj.get(Fields.Transaction.CURRENCY)))
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
