package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.hybrid;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingV31Constants.Time.DEFAULT_OFFSET;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Period;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.ScaExpirationValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.ScaStatus;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.range.DateRangeCalculator;
import se.tink.backend.aggregation.nxgen.core.account.Account;

@RunWith(MockitoJUnitRunner.class)
public class UkObDateCalculatorTest {

    private static final Period maxPeriodForExpiredSca = Period.ofDays(90);
    private static final Period maxPeriod = Period.ofYears(2);

    private final OffsetDateTime constantNow =
            new ConstantLocalDateTimeSource().now().atOffset(DEFAULT_OFFSET);

    private UkObDateCalculator<Account> ukCalculator;

    @Mock private ScaExpirationValidator scaValidator;
    @Mock private DateRangeCalculator<Account> calculator;
    @Mock private Account account;

    @Test
    public void shouldCalculateFinalFromDateWithMaximumRange() {
        // given
        final OffsetDateTime proposedFinalFromDate =
                constantNow.minus(maxPeriod).with(LocalTime.MIN);

        given(scaValidator.evaluateStatus()).willReturn(ScaStatus.VALID);
        given(calculator.calculateFromAsStartOfTheDay(eq(constantNow), eq(maxPeriod)))
                .willReturn(proposedFinalFromDate);
        given(calculator.applyCertainDateLimit(account, proposedFinalFromDate))
                .willReturn(proposedFinalFromDate);

        ukCalculator =
                new UkObDateCalculator<>(
                        scaValidator, calculator, maxPeriodForExpiredSca, maxPeriod);

        // when
        OffsetDateTime finalFromDate = ukCalculator.calculateFinalFromDate(account, constantNow);

        // then
        verify(calculator, times(1)).calculateFromAsStartOfTheDay(eq(constantNow), eq(maxPeriod));
        verify(calculator, times(1)).applyCertainDateLimit(eq(account), eq(proposedFinalFromDate));
        verifyNoMoreInteractions(calculator);
        assertThat(finalFromDate).isEqualTo(proposedFinalFromDate);
    }

    @Test
    public void shouldCalculateFinalFromDateWithMaximumRangeForExpiredSca() {
        // given
        final OffsetDateTime proposedFinalFromDate =
                constantNow.minus(maxPeriodForExpiredSca).with(LocalTime.MIN);

        given(scaValidator.evaluateStatus()).willReturn(ScaStatus.EXPIRED);
        given(calculator.calculateFromAsStartOfTheDay(eq(constantNow), eq(maxPeriodForExpiredSca)))
                .willReturn(proposedFinalFromDate);
        given(calculator.applyCertainDateLimit(account, proposedFinalFromDate))
                .willReturn(proposedFinalFromDate);

        ukCalculator =
                new UkObDateCalculator<>(
                        scaValidator, calculator, maxPeriodForExpiredSca, maxPeriod);

        // when
        OffsetDateTime finalFromDate = ukCalculator.calculateFinalFromDate(account, constantNow);

        // then
        verify(calculator, times(1))
                .calculateFromAsStartOfTheDay(eq(constantNow), eq(maxPeriodForExpiredSca));
        verify(calculator, times(1)).applyCertainDateLimit(eq(account), eq(proposedFinalFromDate));
        verifyNoMoreInteractions(calculator);
        assertThat(finalFromDate).isEqualTo(proposedFinalFromDate);
    }

    @Test
    public void shouldCalculateFinalFromDateWithMaximumRangeForExpiredScaAndLimitedByCertainDate() {
        // given
        final OffsetDateTime proposedFinalFromDate =
                constantNow.minus(maxPeriodForExpiredSca).with(LocalTime.MIN);
        final OffsetDateTime certainDate = constantNow.minusDays(30);

        given(scaValidator.evaluateStatus()).willReturn(ScaStatus.EXPIRED);
        given(calculator.calculateFromAsStartOfTheDay(eq(constantNow), eq(maxPeriodForExpiredSca)))
                .willReturn(proposedFinalFromDate);
        given(calculator.applyCertainDateLimit(account, proposedFinalFromDate))
                .willReturn(certainDate);

        ukCalculator =
                new UkObDateCalculator<>(
                        scaValidator, calculator, maxPeriodForExpiredSca, maxPeriod);

        // when
        OffsetDateTime finalFromDate = ukCalculator.calculateFinalFromDate(account, constantNow);

        // then
        verify(calculator, times(1))
                .calculateFromAsStartOfTheDay(eq(constantNow), eq(maxPeriodForExpiredSca));
        verify(calculator, times(1)).applyCertainDateLimit(eq(account), eq(proposedFinalFromDate));
        verifyNoMoreInteractions(calculator);
        assertThat(finalFromDate).isEqualTo(certainDate);
    }
}
