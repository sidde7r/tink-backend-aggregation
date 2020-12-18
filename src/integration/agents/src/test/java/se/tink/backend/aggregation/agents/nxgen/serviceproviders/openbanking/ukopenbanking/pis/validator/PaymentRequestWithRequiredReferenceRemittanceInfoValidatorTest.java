package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.validator.PaymentRequestWithRequiredReferenceRemittanceInfoValidator.ERROR_MESSAGE;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class PaymentRequestWithRequiredReferenceRemittanceInfoValidatorTest {

    private PaymentRequestWithRequiredReferenceRemittanceInfoValidator paymentRequestValidator;

    @Before
    public void setUp() {
        paymentRequestValidator = new PaymentRequestWithRequiredReferenceRemittanceInfoValidator();
    }

    @Test
    public void testValidRemittanceInformation() {
        // given
        final RemittanceInformation remittanceInformation = createValidRemittanceInformation();
        final PaymentRequest paymentRequest = createPaymentRequest(remittanceInformation);

        // when
        final Throwable thrown =
                catchThrowable(() -> paymentRequestValidator.validate(paymentRequest));

        // then
        assertThat(thrown).isNull();
    }

    @Test
    public void testInvalidRemittanceInformation() {
        // given
        final RemittanceInformation remittanceInformation = createInvalidRemittanceInformation();
        final PaymentRequest paymentRequest = createPaymentRequest(remittanceInformation);

        // when
        final Throwable thrown =
                catchThrowable(() -> paymentRequestValidator.validate(paymentRequest));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(TransferExecutionException.class)
                .hasNoCause()
                .hasMessage(ERROR_MESSAGE);
    }

    @Test
    public void testEmptyRemittanceInformation() {
        // given
        final RemittanceInformation remittanceInformation = createEmptyRemittanceInformation();
        final PaymentRequest paymentRequest = createPaymentRequest(remittanceInformation);

        // when
        final Throwable thrown =
                catchThrowable(() -> paymentRequestValidator.validate(paymentRequest));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(TransferExecutionException.class)
                .hasNoCause()
                .hasMessage(ERROR_MESSAGE);
    }

    private PaymentRequest createPaymentRequest(RemittanceInformation remittanceInformation) {
        final Payment payment = createPayment(remittanceInformation);
        final PaymentRequest paymentRequestMock = mock(PaymentRequest.class);

        when(paymentRequestMock.getPayment()).thenReturn(payment);

        return paymentRequestMock;
    }

    private Payment createPayment(RemittanceInformation remittanceInformation) {
        final Payment paymentMock = mock(Payment.class);

        when(paymentMock.getRemittanceInformation()).thenReturn(remittanceInformation);

        return paymentMock;
    }

    private RemittanceInformation createValidRemittanceInformation() {
        final RemittanceInformation remittanceInformationMock = mock(RemittanceInformation.class);

        when(remittanceInformationMock.getValue()).thenReturn("Valid info");

        return remittanceInformationMock;
    }

    private RemittanceInformation createInvalidRemittanceInformation() {
        final RemittanceInformation remittanceInformationMock = mock(RemittanceInformation.class);

        when(remittanceInformationMock.getValue())
                .thenReturn("Invalid info, because it is too long");

        return remittanceInformationMock;
    }

    private RemittanceInformation createEmptyRemittanceInformation() {
        return mock(RemittanceInformation.class);
    }
}
