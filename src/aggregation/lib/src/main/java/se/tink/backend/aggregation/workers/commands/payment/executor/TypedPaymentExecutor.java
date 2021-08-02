package se.tink.backend.aggregation.workers.commands.payment.executor;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.TypedPaymentControllerable;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.workers.commands.exceptions.TransferAgentWorkerCommandExecutionException;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.rpc.Transfer;

class TypedPaymentExecutor extends BasicPaymentExecutor<TypedPaymentControllerable> {

    TypedPaymentExecutor(Executor executor) {
        super(executor);
    }

    @Override
    protected ExecutorResult execute(
            TypedPaymentControllerable agent,
            TransferRequest transferRequest,
            Credentials credentials)
            throws TransferAgentWorkerCommandExecutionException {
        try {
            Payment payment = handlePayment(transferRequest.getTransfer(), agent, credentials);
            return ExecutorResult.builder().payment(payment).build();
        } catch (PaymentException paymentException) {
            throw new TransferAgentWorkerCommandExecutionException(paymentException);
        }
    }

    @Override
    protected boolean canHandlePayment(Object agent, TransferRequest transferRequest) {
        return agent instanceof TypedPaymentControllerable;
    }

    private Payment handlePayment(
            Transfer transfer,
            TypedPaymentControllerable typedPaymentControllerable,
            Credentials credentials)
            throws PaymentException {

        PaymentController paymentController =
                typedPaymentControllerable
                        .getPaymentController(PaymentRequest.of(transfer).getPayment())
                        .get();
        return handlePayment(paymentController, transfer, credentials);
    }
}
