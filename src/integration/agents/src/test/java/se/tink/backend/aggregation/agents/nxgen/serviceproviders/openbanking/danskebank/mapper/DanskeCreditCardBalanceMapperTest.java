package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.BalanceFixtures.availableCreditLine;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.BalanceFixtures.closingBookedBalance;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.BalanceFixtures.interimAvailableBalance;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.BalanceFixtures.previouslyClosedBookedBalance;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.BalanceFixtures.temporaryCreditLine;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.CreditLineEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards.DefaultCreditCardBalanceMapper;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

public class DanskeCreditCardBalanceMapperTest {

    private DanskeCreditCardBalanceMapper danskeBalanceMapper;
    private DefaultCreditCardBalanceMapper defaultBalanceMapper;
    private PrioritizedValueExtractor valueExtractor;

    @Before
    public void setUp() {
        valueExtractor = Mockito.mock(PrioritizedValueExtractor.class);
        defaultBalanceMapper = new DefaultCreditCardBalanceMapper(valueExtractor);
        danskeBalanceMapper = new DanskeCreditCardBalanceMapper(defaultBalanceMapper);
    }

    @Test
    public void should_return_zero_balance_for_no_credit_line() {
        // given
        AccountBalanceEntity balance1 = previouslyClosedBookedBalance();
        balance1.setCreditLine(null);
        AccountBalanceEntity balance2 = interimAvailableBalance();
        balance2.setCreditLine(Collections.emptyList());

        // when
        ExactCurrencyAmount availableCredit =
                danskeBalanceMapper.getAvailableCredit(ImmutableList.of(balance1, balance2));

        // then
        assertThat(availableCredit.getDoubleValue()).isEqualTo(0d);
        assertThat(availableCredit.getCurrencyCode()).isEqualTo("GBP");
    }

    @Test
    public void should_return_balance_when_one_balance_has_credit_line_and_other_not() {
        // given
        CreditLineEntity returnedCreditLine = availableCreditLine();
        List<CreditLineEntity> creditLines = ImmutableList.of(returnedCreditLine);

        AccountBalanceEntity balance1 = previouslyClosedBookedBalance();
        balance1.setCreditLine(creditLines);
        AccountBalanceEntity balance2 = interimAvailableBalance();
        balance2.setCreditLine(Collections.emptyList());

        // when
        ArgumentCaptor<ImmutableList<UkOpenBankingApiDefinitions.AccountBalanceType>> argument =
                ArgumentCaptor.forClass(ImmutableList.class);
        when(valueExtractor.pickByValuePriority(eq(creditLines), any(), argument.capture()))
                .thenReturn(Optional.of(returnedCreditLine));
        ExactCurrencyAmount availableCredit =
                danskeBalanceMapper.getAvailableCredit(ImmutableList.of(balance1, balance2));

        // then
        assertThat(availableCredit)
                .isEqualTo(
                        ExactCurrencyAmount.of(
                                returnedCreditLine.getAmount().getUnsignedAmount(),
                                returnedCreditLine.getAmount().getCurrency()));
    }

    @Test
    public void should_get_available_credit_from_available_or_preagreed_credit_line() {
        // given
        ImmutableList<CreditLineEntity> creditLines =
                ImmutableList.of(availableCreditLine(), temporaryCreditLine());
        AccountBalanceEntity balanceWithCreditLines = closingBookedBalance();
        balanceWithCreditLines.setCreditLine(creditLines);

        CreditLineEntity returnedCreditLine = availableCreditLine();

        // when
        ArgumentCaptor<ImmutableList<UkOpenBankingApiDefinitions.AccountBalanceType>> argument =
                ArgumentCaptor.forClass(ImmutableList.class);
        when(valueExtractor.pickByValuePriority(eq(creditLines), any(), argument.capture()))
                .thenReturn(Optional.of(returnedCreditLine));

        ExactCurrencyAmount result =
                danskeBalanceMapper.getAvailableCredit(
                        ImmutableList.of(balanceWithCreditLines, previouslyClosedBookedBalance()));

        // then
        assertThat(argument.getValue())
                .asList()
                .isEqualTo(
                        ImmutableList.of(
                                UkOpenBankingApiDefinitions.ExternalLimitType.AVAILABLE,
                                UkOpenBankingApiDefinitions.ExternalLimitType.PRE_AGREED,
                                UkOpenBankingApiDefinitions.ExternalLimitType.CREDIT));

        assertThat(result)
                .isEqualTo(
                        ExactCurrencyAmount.of(
                                returnedCreditLine.getAmount().getUnsignedAmount(),
                                returnedCreditLine.getAmount().getCurrency()));
    }
}
