package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.card;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasApiBaseClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.entities.AccountsItemEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

@RequiredArgsConstructor
public class BnpParibasCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionDatePaginator<CreditCardAccount> {

    private static final long MAX_NUM_MONTHS_FOR_FETCH = 13L;

    private final BnpParibasApiBaseClient bnpParibasApiBaseClient;
    private final LocalDateTimeSource localDateTimeSource;

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return Optional.ofNullable(bnpParibasApiBaseClient.fetchAccounts())
                .map(AccountsResponse::getAccounts).orElseGet(Collections::emptyList).stream()
                .filter(AccountsItemEntity::isCreditCard)
                .map(
                        acc ->
                                acc.toTinkCreditCard(
                                        bnpParibasApiBaseClient.getBalance(acc.getResourceId())))
                .collect(Collectors.toList());
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            CreditCardAccount account, Date fromDate, Date toDate) {

        final LocalDate fromDateLocal = getLocalDateFromDate(fromDate);
        final LocalDate toDateLocal = getLocalDateFromDate(toDate);

        final LocalDate oldestDateForFetch = getOldestDateForTransactionFetch();

        if (oldestDateForFetch.isAfter(toDateLocal)) {
            return PaginatorResponseImpl.createEmpty(false);
        }

        final LocalDate limitedFromDate =
                oldestDateForFetch.isAfter(fromDateLocal) ? oldestDateForFetch : fromDateLocal;

        return bnpParibasApiBaseClient.getTransactions(
                account.getApiIdentifier(), limitedFromDate, toDateLocal);
    }

    private LocalDate getOldestDateForTransactionFetch() {
        return localDateTimeSource
                .now()
                .toLocalDate()
                .minusMonths(MAX_NUM_MONTHS_FOR_FETCH)
                .plusDays(1L);
    }

    private LocalDate getLocalDateFromDate(Date date) {
        return date != null ? date.toInstant().atZone(ZoneId.of("CET")).toLocalDate() : null;
    }
}
