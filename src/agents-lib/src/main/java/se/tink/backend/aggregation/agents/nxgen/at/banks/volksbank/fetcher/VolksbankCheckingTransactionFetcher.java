package se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.fetcher;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.VolksbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.VolksbankConstants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.core.Amount;
import se.tink.libraries.strings.StringUtils;

public class VolksbankCheckingTransactionFetcher
        implements TransactionDatePaginator<TransactionalAccount>, PaginatorResponse {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final Pattern TRANSACTION_INFO = Pattern.compile(
            "<.*Buchungsdatum:[\\s\\S]*?>(?<date>\\d{2}.\\d{2}.\\d{4})[\\s\\S]*?value\">(?<amount>.*(?=<)|.*(?=\\s*<))[\\s\\S]*?unit\">(?<currency>.*(?=<)|.*(?=\\s*<))[\\s\\S]*?buchungstext-line-0\">(?<description>.*(?=<)|.*(?=\\s*<))");

    private final VolksbankApiClient apiClient;
    private List<Transaction> transactions;
    private boolean doSelectAccount = true;

    public VolksbankCheckingTransactionFetcher(VolksbankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, Date fromDate, Date toDate) {

        // TODO for multiple accounts, match account with product Id
        if (doSelectAccount) {
            apiClient.postMainSelectAccount(account.getAccountNumber());
            apiClient.postMainFetchTransactionGeneralCustom();
            doSelectAccount = false;
        }
        this.transactions = new ArrayList<>();
        apiClient.postMainFetchTransactionForDateChange(fromDate, toDate);
        HttpResponse postMainFetchTransactionForDate = apiClient
                .postMainFetchTransactionForDateAction(fromDate, toDate);

        Matcher matcher = TRANSACTION_INFO.matcher(postMainFetchTransactionForDate.getBody(String.class));

        while (matcher.find()) {
            Transaction transaction = Transaction.builder()
                    .setAmount(new Amount(matcher.group(VolksbankConstants.REGEX_GROUP.CURRENCY), StringUtils
                            .parseAmount(matcher.group(VolksbankConstants.REGEX_GROUP.AMOUNT))))
                    .setDate(matcher.group(VolksbankConstants.REGEX_GROUP.DATE), DATE_FORMATTER)
                    .setDescription(matcher.group(VolksbankConstants.REGEX_GROUP.DESCRIPTION))
                    .build();
            this.transactions.add(transaction);
        }

        return this;
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions.isEmpty() ? Collections.emptyList() : transactions;
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(!transactions.isEmpty());
    }
}
