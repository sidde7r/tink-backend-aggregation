package se.tink.backend.aggregation.agents.nxgen.demo.banks.password.executor.transfer;

import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.nxgen.controllers.transfer.BankTransferExecutor;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
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
    public Optional<String> executeTransfer(Transfer transfer) {
        Optional<String> sourceAccountName = transfer.getSource().getName();

        if (sourceAccountName.isPresent() && sourceAccountName.get().contains("Savings Account")) {
            // Mock the user cancel for transfers from the saving account
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setEndUserMessage("Cancel on payment signing (test)")
                    .setMessage("Cancel on payment signing (test)")
                    .build();
        }

        return Optional.empty();
    }
}
