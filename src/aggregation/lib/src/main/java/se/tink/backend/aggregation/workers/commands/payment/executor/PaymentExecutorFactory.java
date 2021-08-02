package se.tink.backend.aggregation.workers.commands.payment.executor;

public interface PaymentExecutorFactory {

    Executor createExecutorsChain();
}
