package se.tink.backend.aggregation.workers.commands.payment.executor;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.workers.commands.exceptions.TransferAgentWorkerCommandExecutionException;

public interface Executor {

    ExecutorResult executePayment(
            Object agent, TransferRequest transferRequest, Credentials credentials)
            throws TransferAgentWorkerCommandExecutionException;
}
