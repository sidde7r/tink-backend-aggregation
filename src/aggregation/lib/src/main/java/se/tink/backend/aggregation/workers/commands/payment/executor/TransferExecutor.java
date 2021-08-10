package se.tink.backend.aggregation.workers.commands.payment.executor;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.workers.commands.exceptions.TransferAgentWorkerCommandExecutionException;

class TransferExecutor extends ExecutorBase<se.tink.backend.aggregation.agents.TransferExecutor> {

    TransferExecutor(Executor executor) {
        super(executor);
    }

    @Override
    protected ExecutorResult execute(
            se.tink.backend.aggregation.agents.TransferExecutor agent,
            TransferRequest transferRequest,
            Credentials credentials)
            throws TransferAgentWorkerCommandExecutionException {
        try {
            agent.execute(transferRequest.getTransfer());
        } catch (Exception exception) {
            throw new TransferAgentWorkerCommandExecutionException(exception);
        }
        return ExecutorResult.builder().build();
    }

    @Override
    protected boolean canHandlePayment(Object agent, TransferRequest transferRequest) {
        return agent instanceof se.tink.backend.aggregation.agents.TransferExecutor;
    }
}
