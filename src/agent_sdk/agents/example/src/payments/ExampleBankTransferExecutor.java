package se.tink.agent.agents.example.payments;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.nxgen.controllers.transfer.BankTransferExecutor;
import se.tink.libraries.transfer.rpc.Transfer;

public class ExampleBankTransferExecutor implements BankTransferExecutor {

    @Override
    public Optional<String> executeTransfer(Transfer transfer) throws TransferExecutionException {
        return Optional.empty();
    }
}
