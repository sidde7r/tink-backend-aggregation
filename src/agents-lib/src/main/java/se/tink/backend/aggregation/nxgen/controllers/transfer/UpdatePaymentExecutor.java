package se.tink.backend.aggregation.nxgen.controllers.transfer;

import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.core.transfer.Transfer;

public interface UpdatePaymentExecutor {
    void updatePayment(Transfer transfer) throws TransferExecutionException;
}
