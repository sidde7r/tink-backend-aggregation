package se.tink.backend.aggregation.workers.commands.payment.executor;

public class PaymentExecutorFactoryImpl implements PaymentExecutorFactory {

    @Override
    public Executor createExecutorsChain() {
        Executor transferExecutor = new TransferExecutor(null);
        Executor ngGenExecutor = new TransferNxgenExecutor(transferExecutor);
        Executor paymentExecutor = new PaymentExecutor(ngGenExecutor);
        Executor typedPaymentExecutor = new TypedPaymentExecutor(paymentExecutor);
        return new RecurringPaymentExecutor(typedPaymentExecutor);
    }
}
