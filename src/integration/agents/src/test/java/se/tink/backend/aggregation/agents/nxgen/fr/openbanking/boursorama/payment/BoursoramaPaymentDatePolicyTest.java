package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.libraries.date.CountryDateHelper;
import se.tink.libraries.payment.rpc.Payment;

public class BoursoramaPaymentDatePolicyTest {

    private static final LocalDateTime EXECUTION_DATE = LocalDateTime.of(2021, 1, 1, 0, 0, 0);
    private static final String EXPECTED_EXECUTION_DATE_WITH_BANK_TIME_ZONE =
            "2021-01-01T00:01:00.000+01:00";
    private CountryDateHelper dateHelper;
    private BoursoramaPaymentDatePolicy datePolicy;

    @Before
    public void setup() {
        dateHelper = Mockito.mock(CountryDateHelper.class);
        LocalDateTimeSource localDateTimeSource = Mockito.mock(LocalDateTimeSource.class);
        given(localDateTimeSource.now(any())).willReturn(EXECUTION_DATE);
        datePolicy = new BoursoramaPaymentDatePolicy(dateHelper, localDateTimeSource);
    }

    @Test
    public void shouldUseExecutionDateIfProvidedForInstantPayment() {
        // given
        Payment payment = Mockito.mock(Payment.class);
        given(payment.isSepaInstant()).willReturn(true);
        given(payment.getExecutionDate()).willReturn(EXECUTION_DATE.toLocalDate());

        // when
        String actualExecutionDateWithBankTimeZone =
                datePolicy.getExecutionDateWithBankTimeZone(payment);

        // then
        assertThat(actualExecutionDateWithBankTimeZone)
                .isEqualTo(EXPECTED_EXECUTION_DATE_WITH_BANK_TIME_ZONE);
    }

    @Test
    public void shouldReturnTodayAsExecutionDateIfNotProvidedForInstantPayment() {
        // given
        Payment payment = Mockito.mock(Payment.class);
        given(payment.isSepaInstant()).willReturn(true);
        given(payment.getExecutionDate()).willReturn(null);
        given(dateHelper.getNowAsLocalDate()).willReturn(EXECUTION_DATE.toLocalDate());

        // when
        String actualExecutionDateWithBankTimeZone =
                datePolicy.getExecutionDateWithBankTimeZone(payment);

        // then
        assertThat(actualExecutionDateWithBankTimeZone)
                .isEqualTo(EXPECTED_EXECUTION_DATE_WITH_BANK_TIME_ZONE);
    }

    @Test
    public void shouldReturnExecutionDate1MinAfterStartOfDayForNonInstantPayment() {
        // given
        Payment payment = mock(Payment.class);
        given(payment.isSepaInstant()).willReturn(false);
        LocalDate executionDate = LocalDate.of(2021, 1, 1);
        given(payment.getExecutionDate()).willReturn(executionDate);

        // when
        String executionDateWithBankTimeZone = datePolicy.getExecutionDateWithBankTimeZone(payment);

        // then
        assertThat(executionDateWithBankTimeZone).isEqualTo("2021-01-01T00:01:00.000+01:00");
    }
}
