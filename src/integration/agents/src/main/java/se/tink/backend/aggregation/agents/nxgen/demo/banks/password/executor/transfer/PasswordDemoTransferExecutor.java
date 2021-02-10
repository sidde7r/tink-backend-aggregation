package se.tink.backend.aggregation.agents.nxgen.demo.banks.password.executor.transfer;

import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.nxgen.controllers.transfer.BankTransferExecutor;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.rpc.Transfer;

public class PasswordDemoTransferExecutor implements BankTransferExecutor {
    private final Credentials credentials;

    public PasswordDemoTransferExecutor(Credentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public Optional<String> executeTransfer(Transfer transfer) {

        String providerName = credentials.getProviderName();
        // This block handles PIS only business use case as source-account will be null in request
        if ("uk-test-open-banking-redirect"
                .equals(providerName)) { // This block handles PIS only business
            // use case as source-account will not
            // be sent in request

            // not need to throw exception for success case
        } else if ("uk-test-open-banking-redirect-failed".equals(providerName)) { // FAILED case

            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(
                            "The transfer amount is larger than what is available on the account (test)")
                    .setMessage(
                            "The transfer amount is larger than what is available on the account (test)")
                    .build();
        } else if ("uk-test-open-banking-redirect-cancelled"
                .equals(providerName)) { // CANCELLED case

            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setEndUserMessage("Cancel on payment signing (test)")
                    .setMessage("Cancel on payment signing (test)")
                    .build();
        } else { // This block handles AIS+PIS business use case as source-account will be sent in
            // request
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
        }
        return Optional.empty();
    }
}
