package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.payments;

import static org.mockito.Mockito.when;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.payments.entities.RedsysPaymentType;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;

public class RedsysPaymentTypeTest {
    @Test
    public void shouldReturnRedsysSepaPaymentTypeWhenTinkPaymentTypeIsSepa() {
        Payment payment = Mockito.mock(Payment.class);
        when(payment.getPaymentScheme()).thenReturn(PaymentScheme.SEPA_CREDIT_TRANSFER);

        Assertions.assertThat(RedsysPaymentType.fromTinkPaymentType(payment.getPaymentScheme()))
                .isEqualTo(RedsysPaymentType.SEPA_CREDIT_TRANSFERS.toString());
    }

    @Test
    public void shouldReturnRedsysInstantSepaPaymentTypeWhenTinkPaymentTypeIsSepaInstant() {
        Payment payment = Mockito.mock(Payment.class);
        when(payment.getPaymentScheme()).thenReturn(PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER);

        Assertions.assertThat(RedsysPaymentType.fromTinkPaymentType(payment.getPaymentScheme()))
                .isEqualTo(RedsysPaymentType.INSTANT_SEPA_CREDIT_TRANSFERS.toString());
    }
}
