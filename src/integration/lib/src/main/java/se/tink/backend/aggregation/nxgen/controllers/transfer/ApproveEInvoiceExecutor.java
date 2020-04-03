package se.tink.backend.aggregation.nxgen.controllers.transfer;

import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.libraries.transfer.rpc.Transfer;

public interface ApproveEInvoiceExecutor {
    void approveEInvoice(Transfer transfer) throws TransferExecutionException;
}
