package se.tink.backend.aggregation.nxgen.controllers.transfer;

import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.libraries.transfer.rpc.Transfer;

public interface PaymentExecutor {
    void executePayment(final Transfer transfer) throws TransferExecutionException;
}
