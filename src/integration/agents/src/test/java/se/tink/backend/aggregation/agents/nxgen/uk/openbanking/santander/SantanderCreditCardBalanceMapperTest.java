package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.santander;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.OPENING_CLEARED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.PREVIOUSLY_CLOSED_BOOKED;
import static se.tink.backend.aggregation.agents.nxgen.uk.openbanking.santander.BalanceFixtures.forwardAvailableBalance;
import static se.tink.backend.aggregation.agents.nxgen.uk.openbanking.santander.BalanceFixtures.interimAvailableBalance;
import static se.tink.backend.aggregation.agents.nxgen.uk.openbanking.santander.BalanceFixtures.previouslyClosedBookedBalance;

import com.google.common.collect.ImmutableList;
import java.util.NoSuchElementException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.AccountBalanceType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.account.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.PrioritizedValueExtractor;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class SantanderCreditCardBalanceMapperTest {

    private SantanderCreditCardBalanceMapper balanceMapper;
    private PrioritizedValueExtractor valueExtractor;

    @Before
    public void setUp() {
        valueExtractor = Mockito.mock(PrioritizedValueExtractor.class);
        balanceMapper = new SantanderCreditCardBalanceMapper(valueExtractor);
    }

    @Test
    public void shouldPickBalance_byDefinedPriority() {
        // given
        ImmutableList<AccountBalanceType> definedPriority =
                ImmutableList.of(PREVIOUSLY_CLOSED_BOOKED, OPENING_CLEARED);
        ImmutableList<AccountBalanceEntity> balances =
                ImmutableList.of(
                        forwardAvailableBalance(),
                        previouslyClosedBookedBalance(),
                        interimAvailableBalance());

        // when
        balanceMapper.getAccountBalance(balances);
        ArgumentCaptor<ImmutableList<AccountBalanceType>> argument =
                ArgumentCaptor.forClass(ImmutableList.class);
        verify(valueExtractor).pickByValuePriority(eq(balances), any(), argument.capture());

        // then
        assertThat(argument.getValue()).asList().isEqualTo(definedPriority);
    }

    @Test
    public void shouldGetAvailableCredit_fromForwardAvailableBalance() {
        // given
        ImmutableList<AccountBalanceEntity> balances =
                ImmutableList.of(
                        forwardAvailableBalance(),
                        previouslyClosedBookedBalance(),
                        interimAvailableBalance());

        // when
        ExactCurrencyAmount availableCredit = balanceMapper.getAvailableCredit(balances);

        // then
        assertThat(availableCredit).isEqualTo(forwardAvailableBalance().getAsCurrencyAmount());
    }

    @Test
    public void shouldThrowException_whenNoForwardAvailableBalanceIsPresent() {
        // given
        ImmutableList<AccountBalanceEntity> balances =
                ImmutableList.of(previouslyClosedBookedBalance(), interimAvailableBalance());

        // when
        Throwable thrown = catchThrowable(() -> balanceMapper.getAvailableCredit(balances));

        // then
        assertThat(thrown).isInstanceOf(NoSuchElementException.class);
    }
}
