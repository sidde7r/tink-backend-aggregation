package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.nxgen.controllers.transfer.BankTransferExecutor;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.transfer.rpc.Transfer;

public class SkandiaBankenBankTransferExecutor implements BankTransferExecutor {

    @Override
    public Optional<String> executeTransfer(Transfer transfer) throws TransferExecutionException {
        throw new NotImplementedException(
                "Bank transfer not yet implemented for " + this.getClass().getName());
    }
}
