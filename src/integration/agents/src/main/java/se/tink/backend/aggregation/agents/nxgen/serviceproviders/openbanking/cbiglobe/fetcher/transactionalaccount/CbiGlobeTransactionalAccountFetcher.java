package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiUrlUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobeFetcherApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CbiGlobeTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionPagePaginator<TransactionalAccount> {
    private static final int TRANSACTIONS_DAYS_BACK = 90;

    private final CbiGlobeFetcherApiClient fetcherApiClient;
    private final CbiStorage storage;
    private final String queryValue;
    private final LocalDateTimeSource localDateTimeSource;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        AccountsResponse accountsResponse = storage.getAccountsResponse();

        if (accountsResponse == null) {
            return Collections.emptyList();
        }

        return accountsResponse.getAccounts().stream()
                .map(
                        acc ->
                                acc.toTinkAccount(
                                        fetcherApiClient.getBalances(
                                                CbiUrlUtils.encodeBlankSpaces(
                                                        acc.getResourceId()))))
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public static CbiGlobeTransactionalAccountFetcher createFromBoth(
            CbiGlobeFetcherApiClient fetcherApiClient,
            CbiStorage storage,
            LocalDateTimeSource localDateTimeSource) {
        return new CbiGlobeTransactionalAccountFetcher(
                fetcherApiClient, storage, QueryValues.BOTH, localDateTimeSource);
    }

    public static CbiGlobeTransactionalAccountFetcher createFromBooked(
            CbiGlobeFetcherApiClient fetcherApiClient,
            CbiStorage storage,
            LocalDateTimeSource localDateTimeSource) {
        return new CbiGlobeTransactionalAccountFetcher(
                fetcherApiClient, storage, QueryValues.BOOKED, localDateTimeSource);
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {
        LocalDate toDate = localDateTimeSource.now(ZoneId.of("CET")).toLocalDate();
        LocalDate fromDate = toDate.minusDays(TRANSACTIONS_DAYS_BACK);
        return fetcherApiClient.getTransactions(
                CbiUrlUtils.encodeBlankSpaces(account.getApiIdentifier()),
                fromDate,
                toDate,
                this.queryValue,
                page);
    }
}
