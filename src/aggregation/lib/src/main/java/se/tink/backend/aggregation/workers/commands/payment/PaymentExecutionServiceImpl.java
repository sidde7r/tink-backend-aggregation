package se.tink.backend.aggregation.workers.commands.payment;

import javax.inject.Inject;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.workers.commands.exceptions.TransferAgentWorkerCommandExecutionException;
import se.tink.backend.aggregation.workers.commands.payment.executor.Executor;
import se.tink.backend.aggregation.workers.commands.payment.executor.ExecutorResult;
import se.tink.backend.aggregation.workers.commands.payment.executor.PaymentExecutorFactory;

public class PaymentExecutionServiceImpl implements PaymentExecutionService {

    private final Executor executor;

    @Inject
    public PaymentExecutionServiceImpl(PaymentExecutorFactory paymentExecutorFactory) {
        this.executor = paymentExecutorFactory.createExecutorsChain();
    }

    @Override
    public ExecutorResult executePayment(
            Object agent, Credentials credentials, TransferRequest transferRequest)
            throws TransferAgentWorkerCommandExecutionException {
        return executor.executePayment(agent, transferRequest, credentials);
    }
}
