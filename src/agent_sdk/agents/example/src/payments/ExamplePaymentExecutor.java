package se.tink.agent.agents.example.payments;

import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.nxgen.controllers.transfer.PaymentExecutor;
import se.tink.libraries.transfer.rpc.Transfer;

public class ExamplePaymentExecutor implements PaymentExecutor {

    @Override
    public void executePayment(Transfer transfer) throws TransferExecutionException {
        throw new UnsupportedOperationException();
    }
}
