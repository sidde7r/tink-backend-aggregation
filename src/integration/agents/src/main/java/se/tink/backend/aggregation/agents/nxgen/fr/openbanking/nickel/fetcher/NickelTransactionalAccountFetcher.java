package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.fetcher;

import static java.util.stream.Collectors.collectingAndThen;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.NickelApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.NickelConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.fetcher.entity.NickelAccount;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.fetcher.entity.NickelAccountDetails;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.fetcher.entity.NickelAccountOverview;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.fetcher.entity.NickelOperation;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.utils.NickelErrorHandler;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RequiredArgsConstructor
public class NickelTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionDatePaginator<TransactionalAccount> {

    private final NickelApiClient apiClient;
    private final NickelErrorHandler errorHandler;
    private final LocalDateTimeSource localDateTimeSource;
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        try {
            return apiClient.getAccounts()
                    .orElseThrow(() -> new IllegalStateException("Can't fetch accounts."))
                    .getAccounts().stream()
                    .peek(account -> account.setAccountOverview(fetchOverview(account)))
                    .filter(NickelAccount::getPrimaryAccount)
                    .peek(account -> account.setAccountDetails(fetchDetails(account)))
                    .map(NickelAccount::toTransactionalAccount)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        } catch (RuntimeException e) {
            throw errorHandler.handle(e);
        }
    }

    public boolean beforeEarliestTransactionHistoryDate(Date date) {
        // All transaction information since the payment account was opened
        return date.toInstant()
                .atZone(ZoneId.of("CET"))
                .toLocalDate()
                .isBefore(localDateTimeSource.now(ZoneId.of("CET")).toLocalDate().minusYears(3));
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {

        try {
            if (beforeEarliestTransactionHistoryDate(toDate)) {
                return PaginatorResponseImpl.createEmpty(false);
            }
            return apiClient
                    .getTransactions(
                            account.getApiIdentifier(),
                            formatter.format(fromDate),
                            formatter.format(toDate),
                            account.getFromTemporaryStorage(StorageKeys.ACCESS_TKN))
                    .orElseThrow(() -> new IllegalStateException("Can't fetch transactions."))
                    .getOperations().stream()
                    .filter(operation -> operation.isInPage(fromDate, toDate))
                    .map(NickelOperation::toTinkTransaction)
                    .collect(collectingAndThen(Collectors.toList(), PaginatorResponseImpl::create));
        } catch (RuntimeException e) {
            throw errorHandler.handle(e);
        }
    }

    private NickelAccountDetails fetchDetails(NickelAccount account) {
        try {
            return apiClient
                    .getAccountDetails(account.getAccessToken())
                    .orElseThrow(() -> new IllegalStateException("Can't fetch account details."));
        } catch (RuntimeException e) {
            throw errorHandler.handle(e);
        }
    }

    private NickelAccountOverview fetchOverview(NickelAccount account) {
        try {
            return apiClient
                    .getAccountOverview(account.getNumber(), account.getAccessToken())
                    .orElseThrow(() -> new IllegalStateException("Can't fetch account overview."));
        } catch (RuntimeException e) {
            throw errorHandler.handle(e);
        }
    }
}
