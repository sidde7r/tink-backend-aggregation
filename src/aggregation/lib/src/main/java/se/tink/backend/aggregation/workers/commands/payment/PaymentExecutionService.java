package se.tink.backend.aggregation.workers.commands.payment;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.workers.commands.exceptions.TransferAgentWorkerCommandExecutionException;
import se.tink.backend.aggregation.workers.commands.payment.executor.ExecutorResult;

public interface PaymentExecutionService {

    ExecutorResult executePayment(
            Object agent, Credentials credentials, TransferRequest transferRequest)
            throws TransferAgentWorkerCommandExecutionException;
}
