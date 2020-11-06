package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.nationwide;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static se.tink.backend.aggregation.agents.nxgen.uk.openbanking.nationwide.BalanceFixtures.closingAvailableBalance;
import static se.tink.backend.aggregation.agents.nxgen.uk.openbanking.nationwide.BalanceFixtures.forwardAvailableBalance;
import static se.tink.backend.aggregation.agents.nxgen.uk.openbanking.nationwide.BalanceFixtures.previouslyClosedBookedBalance;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.creditcards.DefaultCreditCardBalanceMapper;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class NationwideCreditCardBalanceMapperTest {

    private NationwideCreditCardBalanceMapper balanceMapper;

    @Before
    public void setUp() {
        DefaultCreditCardBalanceMapper defaultCreditCardBalanceMapper =
                mock(DefaultCreditCardBalanceMapper.class);
        balanceMapper = new NationwideCreditCardBalanceMapper(defaultCreditCardBalanceMapper);
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
        assertThat(returnedBalance).isEqualByComparingTo(ExactCurrencyAmount.zero("GBP"));
    }
}
