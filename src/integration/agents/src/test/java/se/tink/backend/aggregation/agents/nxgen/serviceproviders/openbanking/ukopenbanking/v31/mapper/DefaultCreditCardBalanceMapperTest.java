package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.CLOSING_AVAILABLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.INTERIM_BOOKED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.PREVIOUSLY_CLOSED_BOOKED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.CreditCardFixtures.interimAvailableBalance;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.CreditCardFixtures.previouslyClosedBookedBalance;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.NoSuchElementException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.AccountBalanceType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.ExternalLimitType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.entities.CreditLineEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.account.AccountBalanceEntity;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class DefaultCreditCardBalanceMapperTest {

    private DefaultCreditCardBalanceMapper balanceMapper;
    private PrioritizedValueExtractor valueExtractor;

    @Before
    public void setUp() {
        valueExtractor = Mockito.mock(PrioritizedValueExtractor.class);
        balanceMapper = new DefaultCreditCardBalanceMapper(valueExtractor);
    }

    @Test
    public void shouldPickBalance_byDefinedPriority() {
        // given
        ImmutableList<AccountBalanceType> definedPriority =
                ImmutableList.of(INTERIM_BOOKED, PREVIOUSLY_CLOSED_BOOKED, CLOSING_AVAILABLE);

        ImmutableList<AccountBalanceEntity> balances =
                ImmutableList.of(
                        CreditCardFixtures.closingBookedBalance(),
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
    public void shouldGetAvailableCredit_fromAvailableOrPreagreedCreditLine() {
        // given
        ImmutableList<CreditLineEntity> creditLines =
                ImmutableList.of(
                        CreditCardFixtures.availableCreditLine(),
                        CreditCardFixtures.temporaryCreditLine());

        AccountBalanceEntity balanceWithCreditLines = CreditCardFixtures.closingBookedBalance();
        balanceWithCreditLines.setCreditLine(creditLines);

        // when
        ArgumentCaptor<ImmutableList<AccountBalanceType>> argument =
                ArgumentCaptor.forClass(ImmutableList.class);
        when(valueExtractor.pickByValuePriority(eq(creditLines), any(), argument.capture()))
                .thenReturn(CreditCardFixtures.availableCreditLine());
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
        assertThat(result).isEqualTo(CreditCardFixtures.availableCreditLine().getAmount());
    }

    @Test
    public void shouldThrowException_whenNoAvailableOrPreagreedCreditLineIsPresent() {
        // given
        AccountBalanceEntity balance1 = previouslyClosedBookedBalance();
        balance1.setCreditLine(Collections.emptyList());
        AccountBalanceEntity balance2 = interimAvailableBalance();
        balance2.setCreditLine(Collections.singletonList(CreditCardFixtures.temporaryCreditLine()));

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
