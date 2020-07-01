package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.nationwide;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static se.tink.backend.aggregation.agents.nxgen.uk.openbanking.nationwide.BalanceFixtures.closingAvailableBalance;
import static se.tink.backend.aggregation.agents.nxgen.uk.openbanking.nationwide.BalanceFixtures.forwardAvailableBalance;
import static se.tink.backend.aggregation.agents.nxgen.uk.openbanking.nationwide.BalanceFixtures.previouslyClosedBookedBalance;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountBalanceEntity;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class NationwideCreditCardBalanceMapperTest {

    private NationwideCreditCardBalanceMapper balanceMapper;

    @Before
    public void setUp() {
        balanceMapper = new NationwideCreditCardBalanceMapper();
    }

    @Test
    public void shouldPickClosingAvailableBalanceAsAccountBalance() {
        // given
        List<AccountBalanceEntity> inputBalances =
                ImmutableList.of(
                        closingAvailableBalance(),
                        forwardAvailableBalance(),
                        previouslyClosedBookedBalance());

        // when
        ExactCurrencyAmount returnedBalance = balanceMapper.getAccountBalance(inputBalances);

        // then
        assertThat(returnedBalance).isEqualByComparingTo(closingAvailableBalance().getAmount());
    }

    @Test
    public void ifNoCreditLineIsAvailable_availableCreditShouldBeZero() {
        // given
        List<AccountBalanceEntity> inputBalances =
                ImmutableList.of(
                        closingAvailableBalance(),
                        forwardAvailableBalance(),
                        previouslyClosedBookedBalance());

        // when
        ExactCurrencyAmount returnedBalance = balanceMapper.getAvailableCredit(inputBalances);

        // then
        assertThat(returnedBalance).isEqualByComparingTo(ExactCurrencyAmount.of(0d, "GBP"));
    }

    @Test
    public void shouldThrowException_whenNoAccountBalanceIsAvailable() {
        // when
        Throwable thrown =
                catchThrowable(() -> balanceMapper.getAccountBalance(Collections.emptyList()));

        // then
        assertThat(thrown).isInstanceOf(NoSuchElementException.class);
    }
}
