package se.tink.backend.aggregation.workers.commands.payment.executor;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.PaymentControllerable;
import se.tink.backend.aggregation.agents.TransferExecutorNxgen;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.workers.commands.exceptions.TransferAgentWorkerCommandExecutionException;

class TransferNxgenExecutor extends ExecutorBase<TransferExecutorNxgen> {

    TransferNxgenExecutor(Executor nextExecutor) {
        super(nextExecutor);
    }

    @Override
    protected ExecutorResult execute(
            TransferExecutorNxgen transferExecutorNxgen,
            TransferRequest transferRequest,
            Credentials credentials)
            throws TransferAgentWorkerCommandExecutionException {
        try {
            transferExecutorNxgen.execute(transferRequest.getTransfer());
        } catch (Exception exception) {
            throw new TransferAgentWorkerCommandExecutionException(exception);
        }
        return ExecutorResult.builder().build();
    }

    @Override
    protected boolean canHandlePayment(Object agent, TransferRequest transferRequest) {
        return agent instanceof PaymentControllerable
                && !((PaymentControllerable) agent).getPaymentController().isPresent();
    }
}
