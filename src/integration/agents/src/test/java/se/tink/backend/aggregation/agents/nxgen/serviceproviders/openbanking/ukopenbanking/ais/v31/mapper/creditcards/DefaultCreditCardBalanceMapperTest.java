package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.CLOSING_AVAILABLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.FORWARD_AVAILABLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.INTERIM_AVAILABLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.INTERIM_BOOKED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.PREVIOUSLY_CLOSED_BOOKED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.BalanceFixtures.availableCreditLine;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.BalanceFixtures.closingBookedBalance;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.BalanceFixtures.interimAvailableBalance;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.BalanceFixtures.previouslyClosedBookedBalance;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.BalanceFixtures.temporaryCreditLine;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.ExternalLimitType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.CreditLineEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.BalanceFixtures;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

public class DefaultCreditCardBalanceMapperTest {

    private DefaultCreditCardBalanceMapper balanceMapper;
    private PrioritizedValueExtractor valueExtractor;

    @Before
    public void setUp() {
        valueExtractor = Mockito.mock(PrioritizedValueExtractor.class);
        balanceMapper = new DefaultCreditCardBalanceMapper(valueExtractor);
    }

    @Test
    public void shouldPickBalance_byExpectedPriority() {
        // given
        List inputBalances = Mockito.mock(List.class);
        AccountBalanceEntity balanceToReturn = BalanceFixtures.closingBookedBalance();

        // when
        ArgumentCaptor<List<AccountBalanceType>> argument = ArgumentCaptor.forClass(List.class);
        when(valueExtractor.pickByValuePriority(eq(inputBalances), any(), argument.capture()))
                .thenReturn(Optional.of(balanceToReturn));
        ExactCurrencyAmount returnedBalance = balanceMapper.getAccountBalance(inputBalances);

        // then
        ImmutableList<AccountBalanceType> expectedPriority =
                ImmutableList.of(
                        INTERIM_BOOKED,
                        PREVIOUSLY_CLOSED_BOOKED,
                        INTERIM_AVAILABLE,
                        CLOSING_AVAILABLE,
                        FORWARD_AVAILABLE);
        assertThat(argument.getValue()).asList().isEqualTo(expectedPriority);
        assertThat(returnedBalance).isEqualByComparingTo(balanceToReturn.getAmount());
    }

    @Test
    public void shouldGetAvailableCredit_fromAvailableOrPreagreedCreditLine() {
        // given
        ImmutableList<CreditLineEntity> creditLines =
                ImmutableList.of(availableCreditLine(), temporaryCreditLine());
        AccountBalanceEntity balanceWithCreditLines = closingBookedBalance();
        balanceWithCreditLines.setCreditLine(creditLines);

        CreditLineEntity returnedCreditLine = availableCreditLine();

        // when
        ArgumentCaptor<ImmutableList<AccountBalanceType>> argument =
                ArgumentCaptor.forClass(ImmutableList.class);
        when(valueExtractor.pickByValuePriority(eq(creditLines), any(), argument.capture()))
                .thenReturn(Optional.of(returnedCreditLine));

        ExactCurrencyAmount result =
                balanceMapper.getAvailableCredit(
                        ImmutableList.of(balanceWithCreditLines, previouslyClosedBookedBalance()));

        // then
        assertThat(argument.getValue())
                .asList()
                .isEqualTo(
                        ImmutableList.of(
                                ExternalLimitType.AVAILABLE,
                                ExternalLimitType.PRE_AGREED,
                                ExternalLimitType.CREDIT));

        assertThat(result)
                .isEqualTo(
                        ExactCurrencyAmount.of(
                                returnedCreditLine.getAmount().getUnsignedAmount(),
                                returnedCreditLine.getAmount().getCurrency()));
    }

    @Test
    public void shouldThrowException_whenNoAvailableOrPreagreedCreditLineIsPresent() {
        // given
        AccountBalanceEntity balance1 = previouslyClosedBookedBalance();
        balance1.setCreditLine(Collections.emptyList());
        AccountBalanceEntity balance2 = interimAvailableBalance();
        balance2.setCreditLine(Collections.singletonList(temporaryCreditLine()));

        // when
        DefaultCreditCardBalanceMapper mapper =
                new DefaultCreditCardBalanceMapper(new PrioritizedValueExtractor());
        Throwable thrown =
                catchThrowable(
                        () -> mapper.getAvailableCredit(ImmutableList.of(balance1, balance2)));

        // then
        assertThat(thrown).isInstanceOf(NoSuchElementException.class);
    }
}
