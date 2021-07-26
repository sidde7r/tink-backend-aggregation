package se.tink.backend.aggregation.workers.commands.payment.executor;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.PaymentControllerable;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.workers.commands.exceptions.TransferAgentWorkerCommandExecutionException;

@Slf4j
class PaymentExecutor extends BasicPaymentExecutor<PaymentControllerable> {

    PaymentExecutor(Executor executor) {
        super(executor);
    }

    @Override
    protected ExecutorResult execute(
            PaymentControllerable agent, TransferRequest transferRequest, Credentials credentials)
            throws TransferAgentWorkerCommandExecutionException {
        try {
            return ExecutorResult.builder()
                    .payment(
                            handlePayment(
                                    agent.getPaymentController().get(),
                                    transferRequest.getTransfer(),
                                    credentials))
                    .build();
        } catch (PaymentException paymentException) {
            throw new TransferAgentWorkerCommandExecutionException(paymentException);
        }
    }

    @Override
    protected boolean canHandlePayment(Object agent, TransferRequest transferRequest) {
        return agent instanceof PaymentControllerable
                && ((PaymentControllerable) agent).getPaymentController().isPresent();
    }
}
