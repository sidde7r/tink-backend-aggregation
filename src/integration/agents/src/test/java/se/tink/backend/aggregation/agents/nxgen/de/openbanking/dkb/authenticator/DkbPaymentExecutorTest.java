package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbApiClient;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;

public class DkbPaymentExecutorTest {

    @Test
    public void should_throw_payment_validation_exception_if_payment_schema_is_instant() {
        // given
        DkbApiClient dkbApiClient = mock(DkbApiClient.class);
        PaymentAuthenticatorPreAuth paymentAuthenticatorPreAuth =
                mock(PaymentAuthenticatorPreAuth.class);
        SessionStorage sessionStorage = mock(SessionStorage.class);
        DkbPaymentExecutor dkbPaymentExecutor =
                new DkbPaymentExecutor(dkbApiClient, paymentAuthenticatorPreAuth, sessionStorage);
        Payment payment =
                new Payment.Builder()
                        .withPaymentScheme(PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER)
                        .build();
        PaymentRequest paymentRequest = new PaymentRequest(payment);

        // when
        Throwable throwable = catchThrowable(() -> dkbPaymentExecutor.create(paymentRequest));

        // then
        assertThat(throwable)
                .isInstanceOf(PaymentValidationException.class)
                .hasMessage("Instant payment is not supported");
    }
}
