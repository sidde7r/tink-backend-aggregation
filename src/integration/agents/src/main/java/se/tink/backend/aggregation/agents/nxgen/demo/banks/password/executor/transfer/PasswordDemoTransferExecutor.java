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

        if (sourceAccountName.isPresent()) {
            String accountName = sourceAccountName.get().toLowerCase().replaceAll("\\s+", "");
            if (accountName.contains("checkingaccounttinkzerobalance")) {
                // Mock the payment failure for zero balance checking accounts
                throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                        .setEndUserMessage(
                                "The transfer amount is larger than what is available on the account (test)")
                        .setMessage(
                                "The transfer amount is larger than what is available on the account (test)")
                        .build();
            }

            if (accountName.contains("savingsaccount")) {
                // Mock the user cancel for transfers from the saving accounts
                throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                        .setEndUserMessage("Cancel on payment signing (test)")
                        .setMessage("Cancel on payment signing (test)")
                        .build();
            }
        }

        return Optional.empty();
    }
}
