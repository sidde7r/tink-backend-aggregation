package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.libraries.payment.rpc.Payment;

public class BpceGroupPaymentDatePolicyTest {

    private BpceGroupPaymentDatePolicy datePolicy;

    @Before
    public void setUp() {
        datePolicy = new BpceGroupPaymentDatePolicy("providerName");
    }

    @Test
    public void shouldReturnExecutionDate1MinOlderThanCreationDateForInstantPayment() {
        // given
        Payment payment = mock(Payment.class);
        given(payment.isSepaInstant()).willReturn(true);

        LocalDateTime now = LocalDateTime.of(2021, 1, 1, 1, 1, 1);
        BpceGroupPaymentDatePolicy spyDatePolicy = Mockito.spy(datePolicy);
        given(spyDatePolicy.getCreationDate()).willReturn(now);

        // when
        String executionDateWithBankTimeZone =
                spyDatePolicy.getExecutionDateWithBankTimeZone(payment);

        // then
        assertThat(executionDateWithBankTimeZone).isEqualTo("2021-01-01T01:02:01.000+01:00");
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
