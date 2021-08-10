package se.tink.backend.aggregation.workers.commands.payment.executor;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.TypedPaymentControllerable;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.rpc.RecurringPaymentRequest;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.workers.commands.exceptions.TransferAgentWorkerCommandExecutionException;

@Slf4j
public class TypedRecurringPaymentExecutor
        extends BasicPaymentExecutor<TypedPaymentControllerable> {

    protected TypedRecurringPaymentExecutor(Executor executor) {
        super(executor);
    }

    @Override
    protected boolean canHandlePayment(Object agent, TransferRequest transferRequest) {
        return agent instanceof TypedPaymentControllerable
                && transferRequest instanceof RecurringPaymentRequest
                && createController(agent, transferRequest).isPresent();
    }

    @Override
    protected ExecutorResult execute(
            TypedPaymentControllerable agent,
            TransferRequest transferRequest,
            Credentials credentials)
            throws TransferAgentWorkerCommandExecutionException {
        try {
            PaymentController paymentController = createController(agent, transferRequest).get();
            PaymentResponse paymentResponse =
                    paymentController.create(
                            fromRecurringTransferRequestToPaymentRequest(
                                    (RecurringPaymentRequest) transferRequest));
            PaymentMultiStepResponse signPaymentMultiStepResponse =
                    paymentController.sign(PaymentMultiStepRequest.of(paymentResponse));

            log.info(
                    "Payment step is - {}, Credentials contain - status: {}",
                    signPaymentMultiStepResponse.getStep(),
                    credentials.getStatus());

            return ExecutorResult.builder()
                    .payment(signPaymentMultiStepResponse.getPayment())
                    .build();
        } catch (Exception e) {
            throw new TransferAgentWorkerCommandExecutionException(e);
        }
    }

    private PaymentRequest fromRecurringTransferRequestToPaymentRequest(
            RecurringPaymentRequest recurringPaymentRequest) {
        return PaymentRequest.ofRecurringPayment((recurringPaymentRequest).getRecurringPayment());
    }

    private Optional<PaymentController> createController(
            Object agent, TransferRequest transferRequest) {
        try {
            return ((TypedPaymentControllerable) agent)
                    .getPaymentController(
                            fromRecurringTransferRequestToPaymentRequest(
                                            (RecurringPaymentRequest) transferRequest)
                                    .getPayment());
        } catch (PaymentRejectedException e) {
            log.error("Failed to create payment controller", e);
            return Optional.empty();
        }
    }
}
