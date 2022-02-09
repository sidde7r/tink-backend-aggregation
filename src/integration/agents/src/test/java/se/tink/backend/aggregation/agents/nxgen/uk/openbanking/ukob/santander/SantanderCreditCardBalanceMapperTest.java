package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.santander;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.santander.BalanceFixtures.forwardAvailableBalance;
import static se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.santander.BalanceFixtures.interimAvailableBalance;
import static se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.santander.BalanceFixtures.openingClearedBalance;
import static se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.santander.BalanceFixtures.previouslyClosedBookedBalance;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.UkObBalanceType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountBalanceEntity;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

public class SantanderCreditCardBalanceMapperTest {

    private SantanderCreditCardBalanceMapper balanceMapper;
    private PrioritizedValueExtractor valueExtractor;
    private final List balancesMockedList = Mockito.mock(List.class);

    @Before
    public void setUp() {
        valueExtractor = Mockito.mock(PrioritizedValueExtractor.class);
        balanceMapper = new SantanderCreditCardBalanceMapper(valueExtractor);
    }

    @Test
    public void shouldPickBalanceByDefinedPriority() {
        // given
        AccountBalanceEntity balanceToReturn = openingClearedBalance();
        ArgumentCaptor<List<UkObBalanceType>> argument = ArgumentCaptor.forClass(List.class);
        when(valueExtractor.pickByValuePriority(eq(balancesMockedList), any(), argument.capture()))
                .thenReturn(Optional.of(balanceToReturn));

        // when
        ExactCurrencyAmount returnedBalance = balanceMapper.getAccountBalance(balancesMockedList);

        // then
        assertThat(argument.getValue())
                .asList()
                .isEqualTo(SantanderCreditCardBalanceMapper.ALLOWED_BALANCE_TYPES);
        assertThat(returnedBalance).isEqualByComparingTo(balanceToReturn.getAmount());
    }

    @Test
    public void shouldPickPreviouslyClosedBalance() {
        // given
        AccountBalanceEntity balanceToReturn = previouslyClosedBookedBalance();
        ArgumentCaptor<List<UkObBalanceType>> argument = ArgumentCaptor.forClass(List.class);
        when(valueExtractor.pickByValuePriority(eq(balancesMockedList), any(), argument.capture()))
                .thenReturn(Optional.of(balanceToReturn));

        // when
        ExactCurrencyAmount returnedBalance = balanceMapper.getAccountBalance(balancesMockedList);

        // then
        assertThat(argument.getValue())
                .asList()
                .isEqualTo(SantanderCreditCardBalanceMapper.ALLOWED_BALANCE_TYPES);
        assertThat(returnedBalance).isEqualByComparingTo(balanceToReturn.getAmount());
    }

    @Test
    public void shouldGetAvailableCreditFromForwardAvailableBalance() {
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
    public void shouldThrowExceptionWhenNoAccountBalanceIsAvailable() {
        // given
        when(valueExtractor.pickByValuePriority(anyCollection(), any(), anyList()))
                .thenReturn(Optional.empty());

        // when
        Throwable thrown =
                catchThrowable(() -> balanceMapper.getAccountBalance(balancesMockedList));

        // then
        assertThat(thrown).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    public void shouldThrowExceptionWhenNoForwardAvailableBalanceIsPresent() {
        // given
        when(valueExtractor.pickByValuePriority(anyCollection(), any(), anyList()))
                .thenReturn(Optional.empty());

        // when
        Throwable thrown =
                catchThrowable(() -> balanceMapper.getAvailableCredit(balancesMockedList));

        // then
        assertThat(thrown).isInstanceOf(NoSuchElementException.class);
    }
}
