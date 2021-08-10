package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.fetcher;

import static java.util.stream.Collectors.collectingAndThen;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client.BoursoramaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.TransactionEntity;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.chrono.AvailableDateInformation;

@RequiredArgsConstructor
@Slf4j
public abstract class BoursoramaAccountBaseFetcher<T extends Account>
        implements AccountFetcher<T>, TransactionDatePaginator<T> {

    private static final long MAX_NUM_DAYS_FOR_FETCH_WITHOUT_SCA = 89L;
    private static final String DEBIT_TRANSACTION_CODE = "DBIT";

    protected static final String STATUS_BOOKED = "BOOK";

    protected final BoursoramaApiClient apiClient;
    protected final LocalDateTimeSource localDateTimeSource;

    @Override
    public Collection<T> fetchAccounts() {
        return Optional.ofNullable(apiClient.fetchAccounts().getAccounts())
                .orElse(Collections.emptyList()).stream()
                .filter(this::filterAccountType)
                .map(this::mapToAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public PaginatorResponse getTransactionsFor(T account, Date fromDate, Date toDate) {

        final LocalDate fromDateLocal = getLocalDateFromDate(fromDate);
        final LocalDate toDateLocal = getLocalDateFromDate(toDate);

        final LocalDate oldestDateForFetch = getOldestDateForTransactionFetch();

        if (oldestDateForFetch.isAfter(toDateLocal)) {
            return PaginatorResponseImpl.createEmpty(false);
        }

        final LocalDate limitedFromDate =
                oldestDateForFetch.isAfter(fromDateLocal) ? oldestDateForFetch : fromDateLocal;

        return apiClient.fetchTransactions(account.getApiIdentifier(), limitedFromDate, toDateLocal)
                .getTransactions().stream()
                .map(this::mapTransaction)
                .collect(collectingAndThen(Collectors.toList(), PaginatorResponseImpl::create));
    }

    private Transaction mapTransaction(TransactionEntity transaction) {
        TransactionDates transactionDate =
                TransactionDates.builder()
                        .setValueDate(
                                new AvailableDateInformation().setDate(transaction.getValueDate()))
                        .build();

        return Transaction.builder()
                .addExternalSystemIds(
                        TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                        transaction.getResourceId())
                .setTransactionDates(transactionDate)
                .setAmount(mapTransactionAmount(transaction))
                .setDescription(StringUtils.join(transaction.getRemittanceInformation(), ';'))
                .setDate(transaction.getBookingDate())
                .setRawDetails(transaction.getEntryReference())
                .setPending(!STATUS_BOOKED.equals(transaction.getStatus()))
                .build();
    }

    protected abstract boolean filterAccountType(AccountEntity accountEntity);

    protected abstract Optional<T> mapToAccount(AccountEntity accountEntity);

    private LocalDate getLocalDateFromDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private LocalDate getOldestDateForTransactionFetch() {
        return localDateTimeSource
                .now()
                .toLocalDate()
                .minusDays(MAX_NUM_DAYS_FOR_FETCH_WITHOUT_SCA);
    }

    private ExactCurrencyAmount mapTransactionAmount(TransactionEntity transaction) {
        ExactCurrencyAmount amount =
                ExactCurrencyAmount.of(
                        transaction.getTransactionAmount().getAmount(),
                        transaction.getTransactionAmount().getCurrency());

        return DEBIT_TRANSACTION_CODE.equals(transaction.getCreditDebitIndicator())
                ? amount.negate()
                : amount;
    }
}
