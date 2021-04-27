package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.fetcher.transactionalaccount;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbStorage;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class DkbTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionDatePaginator<TransactionalAccount> {

    private final DkbApiClient apiClient;
    private final DkbStorage dkbStorage;

    public DkbTransactionalAccountFetcher(DkbApiClient apiClient, DkbStorage dkbStorage) {
        this.apiClient = apiClient;
        this.dkbStorage = dkbStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.getAccounts().getAccounts().stream()
                .map(this::toTinkAccountWithBalance)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<TransactionalAccount> toTinkAccountWithBalance(AccountEntity accountEntity) {
        accountEntity.setBalances(getAccountBalance(accountEntity));
        return accountEntity.toTinkAccount();
    }

    private List<BalanceEntity> getAccountBalance(AccountEntity accountEntity) {
        return apiClient.getBalances(accountEntity.getResourceId()).getBalances();
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {

        final GetTransactionsResponse transactions =
                apiClient.getTransactions(
                        account,
                        getUpdatedFromDateForFetchingTransactions(fromDate, toDate),
                        toDate);

        return PaginatorResponseImpl.create(transactions.toTinkTransactions(), false);
    }

    private Date getUpdatedFromDateForFetchingTransactions(Date fromDate, Date toDate) {
        return isConsentNotPresentOrLessThan60MinutesOld()
                ? convertToDate(convertToLocalDate(toDate).minusDays(730))
                : fromDate;
    }

    // DKB supports fetching transactions further back than 90 days if consent was given within
    // 60 minutes
    private boolean isConsentNotPresentOrLessThan60MinutesOld() {
        if (!dkbStorage.getConsentId().isPresent()) {
            return true;
        } else {
            return dkbStorage
                    .getConsentCreationTime()
                    .map(
                            consentCreationTime ->
                                    ChronoUnit.MINUTES.between(
                                                    consentCreationTime, LocalDateTime.now())
                                            < 60)
                    .orElse(false);
        }
    }

    public static LocalDate convertToLocalDate(Date dateToConvert) {
        return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static Date convertToDate(LocalDate dateToConvert) {
        return Date.from(dateToConvert.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }
}
