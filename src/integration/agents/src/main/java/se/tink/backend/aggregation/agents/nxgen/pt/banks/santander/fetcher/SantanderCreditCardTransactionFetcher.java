package se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class SantanderCreditCardTransactionFetcher
        implements TransactionPagePaginator<CreditCardAccount> {

    private static final int PAGE_SIZE = 1000;
    private static final Pattern TRANSACTION_AMOUNT_PATTERN = Pattern.compile("^-?\\d+,?\\d*");

    private final SantanderApiClient apiClient;

    public SantanderCreditCardTransactionFetcher(SantanderApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, int page) {
        List<Map<String, String>> businessData =
                apiClient
                        .fetchCreditCardTransactions(account.getApiIdentifier(), page, PAGE_SIZE)
                        .getBusinessData();

        return businessData.stream()
                .map(transaction -> toTinkTransaction(transaction, account))
                .collect(
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                transactions ->
                                        PaginatorResponseImpl.create(
                                                transactions, !transactions.isEmpty())));
    }

    private Transaction toTinkTransaction(
            Map<String, String> transaction, CreditCardAccount account) {
        DateTimeFormatter dateTimeFormatter =
                DateTimeFormatter.ofPattern(SantanderConstants.DATE_FORMAT);

        return CreditCardTransaction.builder()
                .setAmount(
                        ExactCurrencyAmount.of(
                                parseAmount(transaction.get(Fields.Transaction.AMOUNT)),
                                account.getFromTemporaryStorage(STORAGE.CURRENCY_CODE)))
                .setCreditAccount(account)
                .setDate(
                        LocalDate.parse(
                                transaction.get(Fields.Transaction.RAW_OPERATION_DATE),
                                dateTimeFormatter))
                .setDescription(transaction.get(Fields.Transaction.DESCRIPTION))
                .build();
    }

    private String parseAmount(String amountString) {
        Matcher matcher = TRANSACTION_AMOUNT_PATTERN.matcher(amountString);
        matcher.find();
        return matcher.group().replace(",", ".");
    }
}
