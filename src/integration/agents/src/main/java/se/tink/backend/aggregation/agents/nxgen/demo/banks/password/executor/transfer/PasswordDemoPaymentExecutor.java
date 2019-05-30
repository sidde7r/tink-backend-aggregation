package se.tink.backend.aggregation.agents.nxgen.demo.banks.password.executor.transfer;

import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.nxgen.controllers.transfer.PaymentExecutor;
import se.tink.libraries.transfer.rpc.Transfer;

public class PasswordDemoPaymentExecutor implements PaymentExecutor {
    public PasswordDemoPaymentExecutor() {}

    @Override
    public void executePayment(Transfer transfer) throws TransferExecutionException {}
}
