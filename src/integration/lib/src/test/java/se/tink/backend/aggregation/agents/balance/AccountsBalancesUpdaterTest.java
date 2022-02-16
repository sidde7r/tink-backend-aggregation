package se.tink.backend.aggregation.agents.balance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.groups.Tuple;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.balance.AccountsBalancesUpdater.Mode;
import se.tink.backend.aggregation.agents.balance.calculators.AvailableBalanceCalculator;
import se.tink.backend.aggregation.agents.balance.calculators.BalanceCalculatorSummary;
import se.tink.backend.aggregation.agents.balance.calculators.BookedBalanceCalculator;
import se.tink.libraries.account_data_cache.AccountData;
import se.tink.libraries.amount.ExactCurrencyAmount;

@RunWith(MockitoJUnitRunner.class)
public class AccountsBalancesUpdaterTest {

    @Mock private Account account;
    @Mock private BookedBalanceCalculator bookedBalanceCalculator;
    @Mock private AvailableBalanceCalculator availableBalanceCalculator;
    private List<AccountData> listOfAccountData;
    private AccountsBalancesUpdater accountsBalancesUpdater;

    @Before
    public void setUp() throws Exception {
        listOfAccountData = getTestListOfAccountData();
        accountsBalancesUpdater =
                new AccountsBalancesUpdater(
                        bookedBalanceCalculator, availableBalanceCalculator, Mode.UPDATING);

        when(account.getGranularAccountBalances()).thenReturn(Maps.newHashMap());
        when(bookedBalanceCalculator.calculateBookedBalance(anyMap(), anyList()))
                .thenReturn(getTestExactCurrencyAmount());
        when(availableBalanceCalculator.calculateAvailableBalance(anyMap(), anyList()))
                .thenReturn(getTestExactCurrencyAmount());
    }

    @Test
    public void shouldNotAffectAccountWhenNullListOfAccountsDataProvided() {
        // given
        // when
        accountsBalancesUpdater.updateAccountsBalancesByRunningCalculations(null);

        // then
        verifyNoImpactOnAccount();
    }

    @Test
    public void shouldCatchExceptionAndLogWarnWhenExceptionHasBeenThrown() {
        // given
        Logger log = (Logger) LoggerFactory.getLogger(AccountsBalancesUpdater.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        log.addAppender(listAppender);
        when(account.getType()).thenReturn(AccountTypes.CHECKING);
        doThrow(new IllegalStateException()).when(account).getExactBalance();

        // when
        accountsBalancesUpdater.updateAccountsBalancesByRunningCalculations(listOfAccountData);

        // then
        verifyNoImpactOnAccount();
        assertThat(listAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage, ILoggingEvent::getLevel)
                .contains(Tuple.tuple("[BALANCE UPDATER] This should be fixed.", Level.ERROR));
    }

    @Test
    public void shouldNotModifyBalancesWhenExceptionThrown() {
        // given
        when(account.getType()).thenReturn(AccountTypes.CHECKING);
        doThrow(new IllegalStateException()).when(account).getExactBalance();

        // when
        accountsBalancesUpdater.updateAccountsBalancesByRunningCalculations(listOfAccountData);

        // then
        verify(account, times(1)).getExactBalance();
        verifyNoImpactOnAccount();
    }

    @Test
    public void shouldUpdateOnlyBookedBalanceByRunningCalculation() {
        // given
        when(account.getType()).thenReturn(AccountTypes.CREDIT_CARD);
        when(account.getExactBalance()).thenReturn(ExactCurrencyAmount.inEUR(50));

        // when
        accountsBalancesUpdater.updateAccountsBalancesByRunningCalculations(listOfAccountData);

        // then
        verifyIfBookedBalanceIsUpdated();
        verifyIfAvailableBalanceIsNotUpdated();
    }

    @Test
    public void shouldUpdateBothBookedAndAvailableBalanceByRunningCalculation() {
        // given
        when(account.getType()).thenReturn(AccountTypes.CHECKING);
        when(account.getExactBalance()).thenReturn(ExactCurrencyAmount.inEUR(50));
        when(account.getAvailableBalance()).thenReturn(ExactCurrencyAmount.inEUR(50));

        // when
        accountsBalancesUpdater.updateAccountsBalancesByRunningCalculations(listOfAccountData);

        // then
        verifyIfBookedBalanceIsUpdated();
        verifyIfAvailableBalanceIsUpdated();
    }

    @Test
    public void shouldNotUpdateAnyBalancesByRunningCalculationIfDryRunEnabled() {
        // given
        accountsBalancesUpdater =
                new AccountsBalancesUpdater(
                        bookedBalanceCalculator, availableBalanceCalculator, Mode.DRY_RUN);
        when(account.getType()).thenReturn(AccountTypes.CHECKING);

        // when
        accountsBalancesUpdater.updateAccountsBalancesByRunningCalculations(listOfAccountData);

        // then
        verifyNoImpactOnAccount();
    }

    private List<AccountData> getTestListOfAccountData() {
        AccountData accountData = new AccountData(account);
        return Lists.newArrayList(accountData);
    }

    private Pair<Optional<ExactCurrencyAmount>, BalanceCalculatorSummary>
            getTestExactCurrencyAmount() {
        ExactCurrencyAmount exactCurrencyAmount = ExactCurrencyAmount.inEUR(100);
        return Pair.of(
                Optional.of(exactCurrencyAmount), BalanceCalculatorSummary.builder().build());
    }

    private void verifyNoImpactOnAccount() {
        verify(account, times(0)).setExactBalance(any());
        verify(account, times(0)).setBalance(any());
        verify(account, times(0)).setAvailableBalance(any());
    }

    private void verifyIfBookedBalanceIsUpdated() {
        verify(account, times(1)).getExactBalance();
        verify(account, times(1)).setExactBalance(any());
        verify(account, times(1)).setBalance(anyDouble());
    }

    private void verifyIfAvailableBalanceIsNotUpdated() {
        verifyIfAvailableBalanceIsUpdated(false);
    }

    private void verifyIfAvailableBalanceIsUpdated() {
        verifyIfAvailableBalanceIsUpdated(true);
    }

    private void verifyIfAvailableBalanceIsUpdated(boolean isUpdated) {
        int wantedNumberOfInvocations = isUpdated ? 1 : 0;
        verify(account, times(wantedNumberOfInvocations)).getAvailableBalance();
        verify(account, times(wantedNumberOfInvocations)).setAvailableBalance(any());
    }
}
