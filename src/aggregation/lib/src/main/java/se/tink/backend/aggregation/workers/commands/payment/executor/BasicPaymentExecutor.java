package se.tink.backend.aggregation.workers.commands.payment.executor;

import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.rpc.Transfer;

@Slf4j
abstract class BasicPaymentExecutor<T> extends ExecutorBase<T> {

    protected BasicPaymentExecutor(Executor executor) {
        super(executor);
    }

    protected Payment handlePayment(
            PaymentController paymentController, Transfer transfer, Credentials credentials)
            throws PaymentException {
        PaymentResponse createPaymentResponse =
                paymentController.create(PaymentRequest.of(transfer));

        log.info("Credentials contain - status: {} before first signing", credentials.getStatus());

        PaymentMultiStepResponse signPaymentMultiStepResponse =
                paymentController.sign(PaymentMultiStepRequest.of(createPaymentResponse));

        log.info("Credentials contain - status: {} after first signing", credentials.getStatus());
        log.info(
                "Payment step is - {} after first signing", signPaymentMultiStepResponse.getStep());

        while (!AuthenticationStepConstants.STEP_FINALIZE.equals(
                signPaymentMultiStepResponse.getStep())) {
            signPaymentMultiStepResponse = sign(paymentController, signPaymentMultiStepResponse);

            log.info("Next step - {}", signPaymentMultiStepResponse.getStep());
            log.info("Credentials contain - status: {}", credentials.getStatus());
        }

        return signPaymentMultiStepResponse.getPayment();
    }

    private PaymentMultiStepResponse sign(
            PaymentController paymentController, PaymentMultiStepResponse paymentMultiStepResponse)
            throws PaymentException {
        return paymentController.sign(
                new PaymentMultiStepRequest(
                        paymentMultiStepResponse.getPayment(),
                        paymentMultiStepResponse.getStorage(),
                        paymentMultiStepResponse.getStep(),
                        Collections.emptyList()));
    }
}
