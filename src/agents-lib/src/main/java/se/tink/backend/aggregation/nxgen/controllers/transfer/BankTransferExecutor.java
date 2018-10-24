package se.tink.backend.aggregation.nxgen.controllers.transfer;

import java.util.Optional;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.core.transfer.Transfer;

public interface BankTransferExecutor {
    Optional<String> executeTransfer(final Transfer transfer) throws TransferExecutionException;
}
