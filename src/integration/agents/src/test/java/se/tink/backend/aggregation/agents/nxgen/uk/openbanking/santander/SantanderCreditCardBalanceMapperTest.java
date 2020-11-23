package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.santander;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.OPENING_CLEARED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.PREVIOUSLY_CLOSED_BOOKED;
import static se.tink.backend.aggregation.agents.nxgen.uk.openbanking.santander.BalanceFixtures.forwardAvailableBalance;
import static se.tink.backend.aggregation.agents.nxgen.uk.openbanking.santander.BalanceFixtures.interimAvailableBalance;
import static se.tink.backend.aggregation.agents.nxgen.uk.openbanking.santander.BalanceFixtures.previouslyClosedBookedBalance;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountBalanceEntity;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

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
        List inputBalances = Mockito.mock(List.class);
        AccountBalanceEntity balanceToReturn = forwardAvailableBalance();

        // when
        ArgumentCaptor<List<AccountBalanceType>> argument = ArgumentCaptor.forClass(List.class);
        when(valueExtractor.pickByValuePriority(eq(inputBalances), any(), argument.capture()))
                .thenReturn(Optional.of(balanceToReturn));
        ExactCurrencyAmount returnedBalance = balanceMapper.getAccountBalance(inputBalances);

        // then
        ImmutableList<AccountBalanceType> expectedPriority =
                ImmutableList.of(PREVIOUSLY_CLOSED_BOOKED, OPENING_CLEARED);
        assertThat(argument.getValue()).asList().isEqualTo(expectedPriority);
        assertThat(returnedBalance).isEqualByComparingTo(balanceToReturn.getAmount());
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
        assertThat(availableCredit).isEqualTo(forwardAvailableBalance().getAmount());
    }

    @Test
    public void shouldThrowException_whenNoAccountBalanceIsAvailable() {
        // when
        when(valueExtractor.pickByValuePriority(anyCollection(), any(), anyList()))
                .thenReturn(Optional.empty());
        Throwable thrown =
                catchThrowable(() -> balanceMapper.getAccountBalance(Mockito.mock(List.class)));

        // then
        assertThat(thrown).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    public void shouldThrowException_whenNoForwardAvailableBalanceIsPresent() {
        // when
        when(valueExtractor.pickByValuePriority(anyCollection(), any(), anyList()))
                .thenReturn(Optional.empty());
        Throwable thrown = catchThrowable(() -> balanceMapper.getAvailableCredit(mock(List.class)));

        // then
        assertThat(thrown).isInstanceOf(NoSuchElementException.class);
    }
}
