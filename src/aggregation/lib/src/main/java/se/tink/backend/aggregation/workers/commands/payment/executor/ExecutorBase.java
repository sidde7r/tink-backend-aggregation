package se.tink.backend.aggregation.workers.commands.payment.executor;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.workers.commands.exceptions.TransferAgentWorkerCommandExecutionException;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
abstract class ExecutorBase<T> implements Executor {

    private final Executor nextExecutor;

    protected ExecutorResult handle(
            Object agent, TransferRequest transferRequest, Credentials credentials)
            throws TransferAgentWorkerCommandExecutionException {
        return nextExecutor != null
                ? nextExecutor.executePayment(agent, transferRequest, credentials)
                : ExecutorResult.builder().build();
    }

    @SuppressWarnings("unchecked")
    public ExecutorResult executePayment(
            Object agent, TransferRequest transferRequest, Credentials credentials)
            throws TransferAgentWorkerCommandExecutionException {
        return canHandlePayment(agent, transferRequest)
                ? execute((T) agent, transferRequest, credentials)
                : handle(agent, transferRequest, credentials);
    }

    protected abstract boolean canHandlePayment(Object agent, TransferRequest transferRequest);

    protected abstract ExecutorResult execute(
            T agent, TransferRequest transferRequest, Credentials credentials)
            throws TransferAgentWorkerCommandExecutionException;
}
