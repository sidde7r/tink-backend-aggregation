package se.tink.backend.aggregation.workers.commands.payment.executor;

public class PaymentExecutorFactoryImpl implements PaymentExecutorFactory {

    @Override
    public Executor createExecutorsChain() {
        Executor ngGenExecutor = new TransferNxgenExecutor(null);
        Executor paymentExecutor = new PaymentExecutor(ngGenExecutor);
        Executor typedPaymentExecutor = new TypedPaymentExecutor(paymentExecutor);
        return new RecurringPaymentExecutor(typedPaymentExecutor);
    }
}
