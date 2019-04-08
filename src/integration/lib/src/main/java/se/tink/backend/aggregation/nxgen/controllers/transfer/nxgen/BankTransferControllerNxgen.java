package se.tink.backend.aggregation.nxgen.controllers.transfer.nxgen;

import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.Optional;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.nxgen.controllers.transfer.BankTransferExecutor;
import se.tink.backend.aggregation.nxgen.controllers.transfer.nxgen.model.Beneficiary;
import se.tink.backend.aggregation.nxgen.controllers.transfer.nxgen.model.OutboxItem;
import se.tink.backend.aggregation.nxgen.controllers.transfer.nxgen.model.TransferEntity;
import se.tink.backend.aggregation.nxgen.controllers.transfer.nxgen.model.TransferSource;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.rpc.Transfer;

public class BankTransferControllerNxgen implements BankTransferExecutor {
    private final BankTransferExecutorNxgen executor;

    public BankTransferControllerNxgen(BankTransferExecutorNxgen executor) {
        Preconditions.checkNotNull(executor, "Executor must not be null");
        this.executor = executor;
    }

    @Override
    public Optional<String> executeTransfer(Transfer transfer) throws TransferExecutionException {
        validateTransfer(transfer);

        executor.initialize();

        // Validate that the outbox is empty
        if (!executor.isOutboxEmpty()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setEndUserMessage(
                            TransferExecutionException.EndUserMessage.EXISTING_UNSIGNED_TRANSFERS
                                    .getKey()
                                    .get())
                    .build();
        }

        // Validate that the source account is one of the customers accounts
        Collection<TransferSource> accounts = executor.getSourceAccounts();

        Optional<TransferSource> source =
                accounts.stream()
                        .filter(
                                account ->
                                        account.getAccountIdentifier().equals(transfer.getSource()))
                        .findFirst();

        if (!source.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(TransferExecutionException.EndUserMessage.SOURCE_NOT_FOUND)
                    .build();
        }

        if (!source.get().isTransferable()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setEndUserMessage(TransferExecutionException.EndUserMessage.INVALID_SOURCE)
                    .build();
        }

        // Validate destination for internal transfers
        TransferEntity transferDestination =
                executor.getInternalTransferDestinations().stream()
                        .filter(x -> x.getAccountIdentifier().equals(transfer.getDestination()))
                        .findFirst()
                        .orElse(null);

        if (transferDestination == null) {
            // Validate destination for external transfers
            Collection<Beneficiary> beneficiaries = executor.getBeneficiaries();

            transferDestination =
                    beneficiaries.stream()
                            .filter(x -> x.getAccountIdentifier().equals(transfer.getDestination()))
                            .findFirst()
                            .orElse(null);

            // Add destination if it isn't found
            if (transferDestination == null) {
                Optional<String> name = transfer.getDestination().getName();

                if (!name.isPresent()) {
                    throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                            .setEndUserMessage(
                                    TransferExecutionException.EndUserMessage
                                            .NEW_RECIPIENT_NAME_ABSENT)
                            .build();
                }

                transferDestination =
                        executor.addBeneficiary(name.get(), transfer.getDestination());
            }
        }

        // Build transfer and add it to the outbox
        OutboxItem outboxItem =
                OutboxItem.builder()
                        .withSource(source.get())
                        .withDestination(transferDestination)
                        .withAmount(transfer.getAmount())
                        .build();

        try {
            executor.addToOutbox(outboxItem);
            executor.signOutbox();
        } catch (Exception e) {
            // Something failed when we tried to add the or sign the transfer. Clean all existing
            // payments from the
            // outbox.
            executor.cleanOutbox();
            throw e; // todo, should we rethrow or catch different errors?
        }

        // Todo: this return an optional status message for a Belgian Bank. TBD on how that should
        // work going forward.
        return Optional.empty();
    }

    private static void validateTransfer(Transfer transfer) {
        AccountIdentifier sourceAccount = transfer.getSource();
        if (sourceAccount == null || !sourceAccount.isValid()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(TransferExecutionException.EndUserMessage.INVALID_SOURCE)
                    .setMessage(BankTransferConstants.ErrorMessage.INVALID_SOURCE)
                    .build();
        }

        AccountIdentifier destinationAccount = transfer.getDestination();
        if (destinationAccount == null || !destinationAccount.isValid()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(
                            TransferExecutionException.EndUserMessage.INVALID_DESTINATION)
                    .setMessage(BankTransferConstants.ErrorMessage.INVALID_DESTINATION)
                    .build();
        }
    }
}
