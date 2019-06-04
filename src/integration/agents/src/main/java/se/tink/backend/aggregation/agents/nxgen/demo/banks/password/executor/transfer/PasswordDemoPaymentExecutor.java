package se.tink.backend.aggregation.agents.nxgen.demo.banks.password.executor.transfer;

import java.util.Optional;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.nxgen.controllers.transfer.PaymentExecutor;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.rpc.Transfer;

public class PasswordDemoPaymentExecutor implements PaymentExecutor {
    public PasswordDemoPaymentExecutor() {}

    @Override
    public void executePayment(Transfer transfer) {
        Optional<String> sourceAccountName = transfer.getSource().getName();

        if (sourceAccountName.isPresent()
                && sourceAccountName
                        .get()
                        .toLowerCase()
                        .replaceAll("\\s+", "")
                        .contains("savingsaccount")) {
            // Mock the user cancel for transfers from the saving account
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setEndUserMessage("Cancel on payment signing (test)")
                    .setMessage("Cancel on payment signing (test)")
                    .build();
        }
    }
}
