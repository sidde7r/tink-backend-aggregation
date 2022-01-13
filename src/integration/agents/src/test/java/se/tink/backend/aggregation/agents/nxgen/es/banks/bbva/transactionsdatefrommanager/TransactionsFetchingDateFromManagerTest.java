package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.transactionsdatefrommanager;

import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RunWith(MockitoJUnitRunner.class)
public class TransactionsFetchingDateFromManagerTest {

    private PersistentStorage persistentStorage = new PersistentStorage();

    @Mock private TransactionPaginationHelper transactionPaginationHelper;

    @Mock private AccountsProvider accountsProvider;

    @Mock private Account account1;

    @Mock private Account account2;

    private TransactionsFetchingDateFromManager objectUnderTest;

    @Before
    public void init() {
        objectUnderTest =
                new TransactionsFetchingDateFromManager(
                        accountsProvider, transactionPaginationHelper, persistentStorage);
        Collection accounts = Lists.newArrayList(account1, account2);
        when(accountsProvider.getAccounts()).thenReturn(accounts);
    }

    @Test
    public void shouldInitDatesFromUsingCertainDate() {
        // given
        LocalDate recentCertainDate = LocalDate.now().minusDays(30);
        when(transactionPaginationHelper.getTransactionDateLimit(account1))
                .thenReturn(
                        Optional.of(
                                Date.from(
                                        recentCertainDate
                                                .atStartOfDay()
                                                .atZone(ZoneId.systemDefault())
                                                .toInstant())));
        when(transactionPaginationHelper.getTransactionDateLimit(account2))
                .thenReturn(Optional.empty());
        objectUnderTest.init();

        // when
        Optional<LocalDate> checkingAccountsDateFrom =
                objectUnderTest.getDateFromForCheckingAccounts();
        Optional<LocalDate> savingsAccountsDateFrom =
                objectUnderTest.getDateFromForSavingsAccounts();
        Optional<LocalDate> creditCardsDateFrom = objectUnderTest.getDateFromForCreditCards();

        // then
        Assertions.assertThat(checkingAccountsDateFrom.get()).isEqualTo(recentCertainDate);
        Assertions.assertThat(savingsAccountsDateFrom.get()).isEqualTo(recentCertainDate);
        Assertions.assertThat(creditCardsDateFrom.get()).isEqualTo(recentCertainDate);
    }

    @Test
    public void allDatesFromShouldBeEmptyWhenThereIsNoSingleCertainDatePresent() {
        // given
        when(transactionPaginationHelper.getTransactionDateLimit(account1))
                .thenReturn(Optional.empty());
        when(transactionPaginationHelper.getTransactionDateLimit(account2))
                .thenReturn(Optional.empty());
        objectUnderTest.init();

        // when
        Optional<LocalDate> checkingAccountsDateFrom =
                objectUnderTest.getDateFromForCheckingAccounts();
        Optional<LocalDate> savingsAccountsDateFrom =
                objectUnderTest.getDateFromForSavingsAccounts();
        Optional<LocalDate> creditCardsDateFrom = objectUnderTest.getDateFromForCreditCards();

        // then
        Assertions.assertThat(checkingAccountsDateFrom).isEmpty();
        Assertions.assertThat(savingsAccountsDateFrom).isEmpty();
        Assertions.assertThat(creditCardsDateFrom).isEmpty();
    }

    @Test
    public void shouldRefreshCheckingAccountsFetchingDate() {
        // given
        LocalDate recentCertainDate = LocalDate.now().minusDays(30);
        when(transactionPaginationHelper.getTransactionDateLimit(account1))
                .thenReturn(
                        Optional.of(
                                Date.from(
                                        recentCertainDate
                                                .atStartOfDay()
                                                .atZone(ZoneId.systemDefault())
                                                .toInstant())));
        when(transactionPaginationHelper.getTransactionDateLimit(account2))
                .thenReturn(Optional.empty());
        objectUnderTest.init();
        objectUnderTest.refreshCheckingAccountsFetchingLastSuccessDate();

        // when
        Optional<LocalDate> checkingAccountsDateFrom =
                objectUnderTest.getDateFromForCheckingAccounts();
        Optional<LocalDate> savingsAccountsDateFrom =
                objectUnderTest.getDateFromForSavingsAccounts();
        Optional<LocalDate> creditCardsDateFrom = objectUnderTest.getDateFromForCreditCards();

        // then
        Assertions.assertThat(checkingAccountsDateFrom.get()).isEqualTo(LocalDate.now());
        Assertions.assertThat(savingsAccountsDateFrom.get()).isEqualTo(recentCertainDate);
        Assertions.assertThat(creditCardsDateFrom.get()).isEqualTo(recentCertainDate);
    }

    @Test
    public void shouldRefreshSavingsAccountsFetchingDate() {
        // given
        LocalDate recentCertainDate = LocalDate.now().minusDays(30);
        when(transactionPaginationHelper.getTransactionDateLimit(account1))
                .thenReturn(
                        Optional.of(
                                Date.from(
                                        recentCertainDate
                                                .atStartOfDay()
                                                .atZone(ZoneId.systemDefault())
                                                .toInstant())));
        when(transactionPaginationHelper.getTransactionDateLimit(account2))
                .thenReturn(Optional.empty());
        objectUnderTest.init();
        objectUnderTest.refreshSavingsAccountsFetchingLastSuccessDate();

        // when
        Optional<LocalDate> checkingAccountsDateFrom =
                objectUnderTest.getDateFromForCheckingAccounts();
        Optional<LocalDate> savingsAccountsDateFrom =
                objectUnderTest.getDateFromForSavingsAccounts();
        Optional<LocalDate> creditCardsDateFrom = objectUnderTest.getDateFromForCreditCards();

        // then
        Assertions.assertThat(checkingAccountsDateFrom.get()).isEqualTo(recentCertainDate);
        Assertions.assertThat(savingsAccountsDateFrom.get()).isEqualTo(LocalDate.now());
        Assertions.assertThat(creditCardsDateFrom.get()).isEqualTo(recentCertainDate);
    }

    @Test
    public void shouldRefreshCreditCardsFetchingDate() {
        // given
        LocalDate recentCertainDate = LocalDate.now().minusDays(30);
        when(transactionPaginationHelper.getTransactionDateLimit(account1))
                .thenReturn(
                        Optional.of(
                                Date.from(
                                        recentCertainDate
                                                .atStartOfDay()
                                                .atZone(ZoneId.systemDefault())
                                                .toInstant())));
        when(transactionPaginationHelper.getTransactionDateLimit(account2))
                .thenReturn(Optional.empty());
        objectUnderTest.init();
        objectUnderTest.refreshCreditCardsFetchingLastSuccessDate();

        // when
        Optional<LocalDate> checkingAccountsDateFrom =
                objectUnderTest.getDateFromForCheckingAccounts();
        Optional<LocalDate> savingsAccountsDateFrom =
                objectUnderTest.getDateFromForSavingsAccounts();
        Optional<LocalDate> creditCardsDateFrom = objectUnderTest.getDateFromForCreditCards();

        // then
        Assertions.assertThat(checkingAccountsDateFrom.get()).isEqualTo(recentCertainDate);
        Assertions.assertThat(savingsAccountsDateFrom.get()).isEqualTo(recentCertainDate);
        Assertions.assertThat(creditCardsDateFrom.get()).isEqualTo(LocalDate.now());
    }

    @Test
    public void shouldThrowIllegalStateExceptionWhenInitMethodNotCalled() {
        // when
        Throwable throwable1 =
                Assertions.catchThrowable(
                        () -> objectUnderTest.refreshCheckingAccountsFetchingLastSuccessDate());
        Throwable throwable2 =
                Assertions.catchThrowable(
                        () -> objectUnderTest.refreshSavingsAccountsFetchingLastSuccessDate());
        Throwable throwable3 =
                Assertions.catchThrowable(
                        () -> objectUnderTest.refreshCreditCardsFetchingLastSuccessDate());
        Throwable throwable4 =
                Assertions.catchThrowable(() -> objectUnderTest.getComputedDateFrom());

        // then
        Assertions.assertThat(throwable1).isInstanceOf(IllegalStateException.class);
        Assertions.assertThat(throwable2).isInstanceOf(IllegalStateException.class);
        Assertions.assertThat(throwable3).isInstanceOf(IllegalStateException.class);
        Assertions.assertThat(throwable4).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void shouldReturnTheOldestRefreshDate() {
        // given
        TransactionsFetchingDateFromManager.BbvaFetchingStatus fetchingStatus =
                TransactionsFetchingDateFromManager.BbvaFetchingStatus.getInstance(
                        persistentStorage);
        LocalDate theOldestDate = LocalDate.now().minusDays(30);
        LocalDate youngerDate = LocalDate.now().minusDays(15);
        fetchingStatus.setCheckingAccountsFetchingLastSuccessDate(youngerDate);
        fetchingStatus.setCreditCardsLastSuccessRefreshDate(theOldestDate);
        objectUnderTest.init();

        // when
        Optional<LocalDate> result = objectUnderTest.getComputedDateFrom();

        // then
        Assertions.assertThat(result.get()).isEqualTo(theOldestDate.minusDays(5));
    }
}
