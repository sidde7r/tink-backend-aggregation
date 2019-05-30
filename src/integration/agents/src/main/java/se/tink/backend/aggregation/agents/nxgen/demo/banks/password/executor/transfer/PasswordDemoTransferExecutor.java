package se.tink.backend.aggregation.agents.nxgen.demo.banks.password.executor.transfer;

import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.nxgen.controllers.transfer.BankTransferExecutor;
import se.tink.libraries.transfer.rpc.Transfer;

public class PasswordDemoTransferExecutor implements BankTransferExecutor {
    private final Credentials credentials;
    private final SupplementalRequester supplementalRequester;

    public PasswordDemoTransferExecutor(
            Credentials credentials, SupplementalRequester supplementalRequester) {
        this.credentials = credentials;
        this.supplementalRequester = supplementalRequester;
    }

    @Override
    public Optional<String> executeTransfer(Transfer transfer) throws TransferExecutionException {
        return Optional.empty();
    }
}
