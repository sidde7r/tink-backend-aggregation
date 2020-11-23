package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.BalanceFixtures.expectedBalance;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.BalanceFixtures.forwardAvailableBalance;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.BalanceFixtures.previouslyClosedBookedBalance;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountBalanceEntity;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class RbsGroupCreditCardBalanceMapperTest {

    private RbsGroupCreditCardBalanceMapper balanceMapper;

    @Before
    public void setUp() {
        balanceMapper = new RbsGroupCreditCardBalanceMapper();
    }

    @Test
    public void shouldPickExpectedBalanceAsAccountBalance() {
        // given
        List<AccountBalanceEntity> inputBalances =
                ImmutableList.of(
                        expectedBalance(),
                        forwardAvailableBalance(),
                        previouslyClosedBookedBalance());

        // when
        ExactCurrencyAmount returnedBalance = balanceMapper.getAccountBalance(inputBalances);

        // then
        assertThat(returnedBalance).isEqualByComparingTo(expectedBalance().getAmount());
    }

    @Test
    public void shouldGetAvailableCredit_fromForwardAvailableBalance() {
        // given
        ImmutableList<AccountBalanceEntity> balances =
                ImmutableList.of(
                        expectedBalance(),
                        forwardAvailableBalance(),
                        previouslyClosedBookedBalance());

        // when
        ExactCurrencyAmount availableCredit = balanceMapper.getAvailableCredit(balances);

        // then
        assertThat(availableCredit).isEqualTo(forwardAvailableBalance().getAmount());
    }

    @Test
    public void shouldThrowException_whenNoAccountBalanceIsAvailable() {
        // when
        Throwable thrown =
                catchThrowable(() -> balanceMapper.getAccountBalance(Collections.emptyList()));

        // then
        assertThat(thrown).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    public void shouldThrowException_whenNoForwardAvailableBalanceIsPresent() {
        // when
        Throwable thrown =
                catchThrowable(() -> balanceMapper.getAvailableCredit(Collections.emptyList()));

        // then
        assertThat(thrown).isInstanceOf(NoSuchElementException.class);
    }
}
