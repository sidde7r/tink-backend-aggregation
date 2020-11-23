package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.transactionalaccounts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.CLOSING_AVAILABLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.CLOSING_BOOKED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.EXPECTED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.FORWARD_AVAILABLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.INTERIM_AVAILABLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.INTERIM_BOOKED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.OPENING_AVAILABLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.OPENING_BOOKED;

import com.google.common.collect.ImmutableList;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.CreditLineEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.BalanceFixtures;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

public class TransactionalAccountBalanceMapperTest {

    private TransactionalAccountBalanceMapper balanceMapper;
    private PrioritizedValueExtractor valueExtractor;

    @Before
    public void setUp() {
        this.valueExtractor = mock(PrioritizedValueExtractor.class);
        this.balanceMapper = new TransactionalAccountBalanceMapper(valueExtractor);
    }

    @Test
    public void shouldPickBalance_accordingToExpectedPriority() {
        // given
        List<AccountBalanceEntity> inputBalances = mock(List.class);
        AccountBalanceEntity expectedBalance = BalanceFixtures.balanceCredit();

        // when
        ArgumentCaptor<List<AccountBalanceType>> argument = ArgumentCaptor.forClass(List.class);
        when(valueExtractor.pickByValuePriority(eq(inputBalances), any(), argument.capture()))
                .thenReturn(Optional.of(expectedBalance));
        ExactCurrencyAmount returnedBalance = balanceMapper.getAccountBalance(inputBalances);

        // then
        ImmutableList<AccountBalanceType> expectedPriority =
                ImmutableList.of(
                        INTERIM_BOOKED,
                        OPENING_BOOKED,
                        CLOSING_BOOKED,
                        EXPECTED,
                        INTERIM_AVAILABLE,
                        OPENING_AVAILABLE,
                        CLOSING_AVAILABLE);
        assertThat(argument.getValue()).asList().isEqualTo(expectedPriority);
        assertThat(returnedBalance).isEqualByComparingTo(expectedBalance.getAmount());
    }

    @Test
    public void shouldThrowException_whenNoAccountBalanceIsAvailable() {
        // when
        when(valueExtractor.pickByValuePriority(anyCollection(), any(), anyList()))
                .thenReturn(Optional.empty());
        Throwable thrown =
                catchThrowable(() -> balanceMapper.getAccountBalance(mock(Collection.class)));

        // then
        assertThat(thrown).isExactlyInstanceOf(NoSuchElementException.class);
    }

    @Test
    public void shouldPickAvailableBalance_accordingToExpectedPriority() {
        // given
        List<AccountBalanceEntity> inputBalances = mock(List.class);
        AccountBalanceEntity expectedBalance = BalanceFixtures.balanceCredit();

        // when
        ArgumentCaptor<List<AccountBalanceType>> argument = ArgumentCaptor.forClass(List.class);
        when(valueExtractor.pickByValuePriority(eq(inputBalances), any(), argument.capture()))
                .thenReturn(Optional.of(expectedBalance));
        Optional<ExactCurrencyAmount> returnedBalance =
                balanceMapper.getAvailableBalance(inputBalances);

        // then
        ImmutableList<AccountBalanceType> expectedPriority =
                ImmutableList.of(
                        INTERIM_AVAILABLE,
                        EXPECTED,
                        FORWARD_AVAILABLE,
                        OPENING_AVAILABLE,
                        CLOSING_AVAILABLE);

        assertThat(argument.getValue()).asList().isEqualTo(expectedPriority);
        assertThat(returnedBalance.get()).isEqualByComparingTo(expectedBalance.getAmount());
    }

    @Test
    public void shouldPickAvailableCredit_fromCreditLineOfType_AVAILABLE() {
        // given
        AccountBalanceEntity debitBalance = BalanceFixtures.balanceDebit();
        AccountBalanceEntity closingBookedBalance = BalanceFixtures.closingBookedBalance();
        CreditLineEntity temporaryCreditLine = BalanceFixtures.temporaryCreditLine();
        CreditLineEntity availableCreditLine = BalanceFixtures.availableCreditLine();
        CreditLineEntity creditCreditLine = BalanceFixtures.creditCreditLine();

        // when
        closingBookedBalance.setCreditLine(
                ImmutableList.of(temporaryCreditLine, availableCreditLine, creditCreditLine));
        Optional<ExactCurrencyAmount> creditLimit =
                balanceMapper.calculateAvailableCredit(
                        ImmutableList.of(debitBalance, closingBookedBalance));

        // then
        assertThat(creditLimit.get())
                .isEqualByComparingTo(
                        ExactCurrencyAmount.of(
                                availableCreditLine.getAmount().getUnsignedAmount(),
                                availableCreditLine.getAmount().getCurrency()));
    }

    @Test
    public void shouldPickCreditLimit_fromCreditLineOfTypeCREDIT() {
        // given
        AccountBalanceEntity debitBalance = BalanceFixtures.balanceDebit();
        AccountBalanceEntity closingBookedBalance = BalanceFixtures.closingBookedBalance();
        CreditLineEntity temporaryCreditLine = BalanceFixtures.temporaryCreditLine();
        CreditLineEntity availableCreditLine = BalanceFixtures.availableCreditLine();
        CreditLineEntity creditCreditLine = BalanceFixtures.creditCreditLine();
        closingBookedBalance.setCreditLine(
                ImmutableList.of(temporaryCreditLine, availableCreditLine, creditCreditLine));

        // when
        Optional<ExactCurrencyAmount> creditLimit =
                balanceMapper.calculateCreditLimit(
                        ImmutableList.of(debitBalance, closingBookedBalance));

        // then
        assertThat(creditLimit.get())
                .isEqualByComparingTo(
                        ExactCurrencyAmount.of(
                                creditCreditLine.getAmount().getUnsignedAmount(),
                                creditCreditLine.getAmount().getCurrency()));

        // when
        Optional<ExactCurrencyAmount> expectedEmptyLimit =
                balanceMapper.calculateCreditLimit(Collections.emptyList());
        // then
        assertThat(expectedEmptyLimit.isPresent()).isFalse();
    }

    @Test
    public void
            ifNo_CREDIT_CreditLineIsPresent_shouldPickCreditLimit_fromSumOf_PREAGREED_and_EMERGENCY_if_PREAGREED_available() {
        // given
        AccountBalanceEntity inputBalance = BalanceFixtures.closingBookedBalance();
        CreditLineEntity temporaryCreditLine = BalanceFixtures.temporaryCreditLine();
        CreditLineEntity emergencyCreditLine = BalanceFixtures.emergencyCreditLine();
        CreditLineEntity preAgreedCreditLine = BalanceFixtures.preAgreedCreditLine();

        // when
        inputBalance.setCreditLine(ImmutableList.of(temporaryCreditLine, preAgreedCreditLine));
        Optional<ExactCurrencyAmount> expectedPreAgreed =
                balanceMapper.calculateCreditLimit(ImmutableList.of(inputBalance));

        // then
        assertThat(expectedPreAgreed.get())
                .isEqualByComparingTo(
                        ExactCurrencyAmount.of(
                                preAgreedCreditLine.getAmount().getUnsignedAmount(),
                                preAgreedCreditLine.getAmount().getCurrency()));

        // when only emergency credit line available
        inputBalance.setCreditLine(ImmutableList.of(temporaryCreditLine, emergencyCreditLine));
        Optional<ExactCurrencyAmount> expectedEmergency =
                balanceMapper.calculateCreditLimit(ImmutableList.of(inputBalance));

        // then  should not pick it - it can only be added on top of pre-agreed
        assertThat(expectedEmergency.isPresent()).isFalse();

        // when both are present
        inputBalance.setCreditLine(
                ImmutableList.of(temporaryCreditLine, emergencyCreditLine, preAgreedCreditLine));
        Optional<ExactCurrencyAmount> expectedSum =
                balanceMapper.calculateCreditLimit(ImmutableList.of(inputBalance));

        // then should add them
        BigDecimal expectedEmergencyAmount =
                new BigDecimal(emergencyCreditLine.getAmount().getUnsignedAmount());
        BigDecimal expectedPreAgreedAmount =
                new BigDecimal(preAgreedCreditLine.getAmount().getUnsignedAmount());

        assertThat(expectedSum.get())
                .isEqualByComparingTo(
                        ExactCurrencyAmount.of(
                                expectedEmergencyAmount.add(expectedPreAgreedAmount), "GBP"));
    }

    @Test
    public void shouldReturnEmptyValue_ifCreditLimitIsImpossibleToCalculate() {
        // given
        AccountBalanceEntity debitBalance = BalanceFixtures.balanceDebit();
        AccountBalanceEntity closingBookedBalance = BalanceFixtures.closingBookedBalance();
        CreditLineEntity temporaryCreditLine = BalanceFixtures.temporaryCreditLine();
        closingBookedBalance.setCreditLine(ImmutableList.of(temporaryCreditLine));

        // when
        Optional<ExactCurrencyAmount> creditLimit =
                balanceMapper.calculateAvailableCredit(
                        ImmutableList.of(debitBalance, closingBookedBalance));

        // then
        assertThat(creditLimit.isPresent()).isFalse();
    }
}
