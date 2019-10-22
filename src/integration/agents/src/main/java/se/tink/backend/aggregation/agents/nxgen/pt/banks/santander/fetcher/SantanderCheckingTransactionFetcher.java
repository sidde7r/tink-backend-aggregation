package se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.SantanderApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.SantanderConstants;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.SantanderConstants.STORAGE;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class SantanderCheckingTransactionFetcher
        implements TransactionDatePaginator<TransactionalAccount> {

    private static final Pattern TRANSACTION_AMOUNT_PATTERN = Pattern.compile("^-?\\d+,?\\d*");
    private static final int PAGE_SIZE = 1000;

    private final SantanderApiClient apiClient;

    public SantanderCheckingTransactionFetcher(SantanderApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {

        LocalDate requestFromDate = convert(fromDate);
        LocalDate requestToDate = convert(toDate);
        boolean canFetchMore = true;

        if (requestFromDate.getYear() < 2019) { // API returns errors for transactions before 2019
            requestFromDate = LocalDate.of(2019, 1, 1);
            canFetchMore = false;
        }

        List<Transaction> transactions = fetchAllForPeriod(account, requestFromDate, requestToDate);
        return PaginatorResponseImpl.create(transactions, canFetchMore);
    }

    private List<Transaction> fetchAllForPeriod(
            TransactionalAccount account, LocalDate fromDate, LocalDate toDate) {

        List<Transaction> allTransactions = new ArrayList<>();

        List<Transaction> transactionPage;
        int currentPage = 1;
        do {
            ApiResponse<Map<String, String>> apiResponse =
                    apiClient.fetchTransactions(
                            account.getAccountNumber(),
                            account.getFromTemporaryStorage(STORAGE.BRANCH_CODE),
                            fromDate,
                            toDate,
                            currentPage,
                            PAGE_SIZE);

            transactionPage =
                    deserializeTransactions(
                            apiResponse.getBusinessData(),
                            account.getFromTemporaryStorage(STORAGE.CURRENCY_CODE));

            allTransactions.addAll(transactionPage);
            currentPage++;
        } while (!transactionPage.isEmpty());

        return allTransactions;
    }

    private List<Transaction> deserializeTransactions(
            List<Map<String, String>> transactions, String currencyCode) {

        DateTimeFormatter dateTimeFormatter =
                DateTimeFormatter.ofPattern(SantanderConstants.DATE_FORMAT);

        return transactions.stream()
                .map(
                        obj ->
                                Transaction.builder()
                                        .setAmount(
                                                ExactCurrencyAmount.of(
                                                        parseAmount(
                                                                obj.get(Fields.Transaction.AMOUNT)),
                                                        currencyCode))
                                        .setDate(
                                                LocalDate.parse(
                                                        obj.get(Fields.Transaction.OPERATION_DATE),
                                                        dateTimeFormatter))
                                        .setDescription(obj.get(Fields.Transaction.DESCRIPTION))
                                        .build())
                .collect(
                        Collectors.collectingAndThen(
                                Collectors.toList(), Collections::unmodifiableList));
    }

    private String parseAmount(String amountString) {
        Matcher matcher = TRANSACTION_AMOUNT_PATTERN.matcher(amountString);
        matcher.find();
        return matcher.group().replace(",", ".");
    }

    private LocalDate convert(Date date) {
        return date.toInstant().atZone(ZoneId.of(SantanderConstants.TIMEZONE_ID)).toLocalDate();
    }
}
