package se.tink.backend.aggregation.workers.commands.payment.executor;

import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.PaymentControllerable;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.rpc.RecurringPaymentRequest;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.workers.commands.exceptions.TransferAgentWorkerCommandExecutionException;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.rpc.RecurringPayment;

@Slf4j
class RecurringPaymentExecutor extends BasicPaymentExecutor<PaymentControllerable> {

    RecurringPaymentExecutor(Executor executor) {
        super(executor);
    }

    public boolean canHandlePayment(Object agent, TransferRequest transferRequest) {
        if (transferRequest instanceof RecurringPaymentRequest) {
            if (agent instanceof PaymentControllerable
                    && ((PaymentControllerable) agent).getPaymentController().isPresent()) {
                return true;
            }
            log.error("Payment not supported by Agent=" + agent.getClass());
            return false;
        }
        return false;
    }

    @Override
    protected ExecutorResult execute(
            PaymentControllerable agent, TransferRequest transferRequest, Credentials credentials)
            throws TransferAgentWorkerCommandExecutionException {
        RecurringPayment recurringPayment =
                ((RecurringPaymentRequest) transferRequest).getRecurringPayment();

        try {
            PaymentController paymentController = agent.getPaymentController().get();
            PaymentResponse createPaymentResponse =
                    paymentController.create(PaymentRequest.ofRecurringPayment(recurringPayment));

            return ExecutorResult.builder()
                    .payment(
                            handleRecurringPaymentSigning(
                                    paymentController, createPaymentResponse, credentials))
                    .build();
        } catch (Exception exception) {
            throw new TransferAgentWorkerCommandExecutionException(exception);
        }
    }

    private Payment handleRecurringPaymentSigning(
            PaymentController paymentController,
            PaymentResponse createPaymentResponse,
            Credentials credentials)
            throws PaymentException {
        PaymentMultiStepResponse signPaymentMultiStepResponse =
                paymentController.sign(PaymentMultiStepRequest.of(createPaymentResponse));

        log.info(
                "Payment step is - {}, Credentials contain - status: {}",
                signPaymentMultiStepResponse.getStep(),
                credentials.getStatus());

        while (!AuthenticationStepConstants.STEP_FINALIZE.equals(
                signPaymentMultiStepResponse.getStep())) {
            signPaymentMultiStepResponse = sign(paymentController, signPaymentMultiStepResponse);

            log.info(
                    "Next step - {}, Credentials status: {}",
                    signPaymentMultiStepResponse.getStep(),
                    credentials.getStatus());
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
