package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.transactionsdatefrommanager;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

/**
 * This class is designed to compute a [date from] for transactions fetching.<br>
 * The idea is to store last refresh dates for checking and savings accounts and for credit cards as
 * well.<br>
 * <b>How the [date from] is computed?</b>
 *
 * <ol>
 *   <li>If it is a first refresh [date from] will be empty
 *   <li>If it is not a first refresh, the [date from] will be the oldest refresh date from checking
 *       accounts, saving accounts and credit cards last success refresh date
 *   <li>For backward compatibility (for already existed credentials) when date from manager is
 *       introduced to the an existing agent, the [date from] will be computed from accounts certain
 *       dates - the youngest date will be used. If there is no certain date set, the [date from]
 *       will be empty
 * </ol>
 *
 * <b>How to use it?</b>
 *
 * <ol>
 *   <li>The <code>AccountsProvider</code> implementation has to provide a set of accounts combined
 *       from: checking accounts, savings accounts and credit cards.
 *   <li>The date returned by <code>getComputedDateFrom()</code> should be used to decide if
 *       extended consent is needed.
 *   <li>The <code>TransactionalAccountRefreshControllerTransactionsFetchingDateFromManagerAware
 *       </code> has to be used as a checking and savings accounts fetching controller.
 *   <li>The <code>CreditCardRefreshControllerTransactionsFetchingDateFromManagerAware</code> has to
 *       be used as a credit cards fetching controller.
 *   <li>During building a fetching transaction request the date returned by <code>
 *       getComputedDateFrom()</code> should be used (if present).
 * </ol>
 */
@RequiredArgsConstructor
@Slf4j
public class TransactionsFetchingDateFromManager {

    private static final int DAYS_WINDOW_FOR_NOT_BOOKED_TRANSACTIONS = 5;

    private final AccountsProvider accountsProvider;
    private final TransactionPaginationHelper transactionPaginationHelper;
    private final PersistentStorage persistentStorage;
    private BbvaFetchingStatus fetchingStatus;

    public Optional<LocalDate> getComputedDateFrom() {
        List<LocalDate> dates = new LinkedList<>();
        getDateFromForCheckingAccounts().ifPresent(dates::add);
        getDateFromForSavingsAccounts().ifPresent(dates::add);
        getDateFromForCreditCards().ifPresent(dates::add);
        Collections.sort(dates);
        return dates.stream()
                .findFirst()
                .map(d -> d.minusDays(DAYS_WINDOW_FOR_NOT_BOOKED_TRANSACTIONS));
    }

    private void init() {
        fetchingStatus = BbvaFetchingStatus.getInstance(persistentStorage);
        findTheMostRecentCertainDateForAccounts()
                .ifPresent(
                        date -> {
                            fetchingStatus.setCheckingAccountsFetchingLastSuccessDate(date);
                            fetchingStatus.setSavingsAccountsLastSuccessRefreshDate(date);
                            fetchingStatus.setCreditCardsLastSuccessRefreshDate(date);
                            log.info(
                                    "Fetching status is already in the place: "
                                            + fetchingStatus.toString());
                        });
        fetchingStatus.save();
    }

    Optional<LocalDate> getDateFromForCheckingAccounts() {
        return getFetchingStatus().getCheckingAccountsLastSuccessRefreshDate();
    }

    Optional<LocalDate> getDateFromForSavingsAccounts() {
        return getFetchingStatus().getSavingsAccountsLastSuccessRefreshDate();
    }

    Optional<LocalDate> getDateFromForCreditCards() {
        return getFetchingStatus().getCreditCardsLastSuccessRefreshDate();
    }

    public void refreshCheckingAccountsFetchingLastSuccessDate() {
        getFetchingStatus().setCheckingAccountsFetchingLastSuccessDate(LocalDate.now());
    }

    public void refreshSavingsAccountsFetchingLastSuccessDate() {
        getFetchingStatus().setSavingsAccountsLastSuccessRefreshDate(LocalDate.now());
    }

    public void refreshCreditCardsFetchingLastSuccessDate() {
        getFetchingStatus().setCreditCardsLastSuccessRefreshDate(LocalDate.now());
    }

    public void cleanCheckingAccountsFetchingLastSuccessDate() {
        fetchingStatus.setCheckingAccountsFetchingLastSuccessDate(null);
    }

    public void cleanSavingsAccountsFetchingLastSuccessDate() {
        fetchingStatus.setSavingsAccountsLastSuccessRefreshDate(null);
    }

    public void cleanCreditCardsFetchingLastSuccessDate() {
        fetchingStatus.setCreditCardsLastSuccessRefreshDate(null);
    }

    private BbvaFetchingStatus getFetchingStatus() {
        if (fetchingStatus == null) {
            init();
        }
        return fetchingStatus;
    }

    private Optional<LocalDate> findTheMostRecentCertainDateForAccounts() {
        return accountsProvider.getAccounts().stream()
                .map(account -> transactionPaginationHelper.getTransactionDateLimit(account))
                .filter(o -> o.isPresent())
                .map(o -> o.get())
                .sorted(Comparator.reverseOrder())
                .findFirst()
                .map(
                        o ->
                                o.toInstant()
                                        .atZone(ZoneId.of(BbvaConstants.Defaults.TIMEZONE_CET))
                                        .toLocalDate());
    }

    @JsonAutoDetect(
            fieldVisibility = JsonAutoDetect.Visibility.ANY,
            getterVisibility = JsonAutoDetect.Visibility.NONE,
            setterVisibility = JsonAutoDetect.Visibility.NONE)
    static class BbvaFetchingStatus {

        @JsonIgnore private PersistentStorage persistentStorage;

        private LocalDate checkingAccountsLastSuccessRefreshDate;
        private LocalDate savingsAccountsLastSuccessRefreshDate;
        private LocalDate creditCardsLastSuccessRefreshDate;

        private BbvaFetchingStatus() {}

        private BbvaFetchingStatus(PersistentStorage persistentStorage) {
            this.persistentStorage = persistentStorage;
        }

        public static BbvaFetchingStatus getInstance(PersistentStorage persistentStorage) {
            return persistentStorage
                    .get(BbvaFetchingStatus.class.getSimpleName(), BbvaFetchingStatus.class)
                    .map(
                            s -> {
                                s.persistentStorage = persistentStorage;
                                return s;
                            })
                    .orElse(new BbvaFetchingStatus(persistentStorage));
        }

        private void save() {
            persistentStorage.put(BbvaFetchingStatus.class.getSimpleName(), this);
        }

        Optional<LocalDate> getCheckingAccountsLastSuccessRefreshDate() {
            return Optional.ofNullable(checkingAccountsLastSuccessRefreshDate);
        }

        Optional<LocalDate> getSavingsAccountsLastSuccessRefreshDate() {
            return Optional.ofNullable(savingsAccountsLastSuccessRefreshDate);
        }

        Optional<LocalDate> getCreditCardsLastSuccessRefreshDate() {
            return Optional.ofNullable(creditCardsLastSuccessRefreshDate);
        }

        void setCheckingAccountsFetchingLastSuccessDate(LocalDate date) {
            checkingAccountsLastSuccessRefreshDate = date;
            save();
        }

        void setSavingsAccountsLastSuccessRefreshDate(LocalDate date) {
            savingsAccountsLastSuccessRefreshDate = date;
            save();
        }

        void setCreditCardsLastSuccessRefreshDate(LocalDate date) {
            creditCardsLastSuccessRefreshDate = date;
            save();
        }

        @Override
        public String toString() {
            return "{ checkingAccountsLastSuccessRefreshDate="
                    + checkingAccountsLastSuccessRefreshDate
                    + ", savingsAccountsLastSuccessRefreshDate="
                    + savingsAccountsLastSuccessRefreshDate
                    + ", creditCardsLastSuccessRefreshDate="
                    + creditCardsLastSuccessRefreshDate
                    + '}';
        }
    }
}
