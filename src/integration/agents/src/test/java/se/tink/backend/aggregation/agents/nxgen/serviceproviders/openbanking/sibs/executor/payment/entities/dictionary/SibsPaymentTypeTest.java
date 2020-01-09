package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.dictionary;

import static org.mockito.Mockito.when;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.libraries.payment.rpc.Payment;

public class SibsPaymentTypeTest {

    @Test
    public void shouldReturnSibsSepaPaymentTypeWhenPaymentTypeIsSepa() {
        Payment payment = Mockito.mock(Payment.class);
        when(payment.isSepa()).thenReturn(true);

        Assertions.assertThat(SibsPaymentType.fromDomainPayment(payment))
                .isEqualTo(SibsPaymentType.SEPA_CREDIT_TRANSFERS);
    }

    @Test
    public void shouldReturnSibsCrossBorderPaymentTypeWhenPaymentTypeIsNotSepa() {
        Payment payment = Mockito.mock(Payment.class);
        when(payment.isSepa()).thenReturn(false);

        Assertions.assertThat(SibsPaymentType.fromDomainPayment(payment))
                .isEqualTo(SibsPaymentType.CROSS_BORDER_CREDIT_TRANSFERS);
    }
}
