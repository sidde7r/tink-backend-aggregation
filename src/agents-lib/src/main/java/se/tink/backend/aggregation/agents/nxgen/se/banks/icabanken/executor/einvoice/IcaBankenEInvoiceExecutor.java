package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.einvoice;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.IcaBankenExecutorHelper;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.einvoice.rpc.AcceptEInvoiceTransferRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.einvoice.rpc.FinishEInvoiceSignRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.einvoice.rpc.UpdateEInvoiceRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.einvoice.rpc.ValidateEInvoiceRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.einvoice.rpc.ValidateEInvoiceResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice.entities.EInvoiceBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice.entities.EInvoiceEntity;
import se.tink.backend.aggregation.nxgen.controllers.transfer.ApproveEInvoiceExecutor;
import se.tink.libraries.amount.Amount;
import se.tink.backend.core.signableoperation.SignableOperationStatuses;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.transfer.enums.TransferPayloadType;
import se.tink.libraries.i18n.Catalog;

public class IcaBankenEInvoiceExecutor implements ApproveEInvoiceExecutor {
    private static final Logger log = LoggerFactory.getLogger(IcaBankenEInvoiceExecutor.class);

    private static final Retryer<ValidateEInvoiceResponse> WHILE_ICABANKEN_CORRECTS_VALIDATION_ERROR_RETRYER =
            RetryerBuilder.<ValidateEInvoiceResponse>newBuilder()
                    .retryIfResult(ValidateEInvoiceResponse::dateInvalidButIcaBankenCorrectedIt)
                    // Upper retry bound is important to avoid infinite loop.
                    .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                    .build();

    private final IcaBankenApiClient apiClient;
    private final IcaBankenExecutorHelper executorHelper;
    private final Catalog catalog;

    public IcaBankenEInvoiceExecutor(IcaBankenApiClient apiClient, IcaBankenExecutorHelper executorHelper,
            Catalog catalog) {
        this.apiClient = apiClient;
        this.executorHelper = executorHelper;
        this.catalog = catalog;
    }

    @Override
    public void approveEInvoice(final Transfer transfer) throws TransferExecutionException {
        executorHelper.validateNoUnsignedTransfers();
        String invoiceId = getInvoiceIdFrom(transfer);

        Collection<AccountEntity> ownAccounts = apiClient.fetchAccounts().getOwnAccounts();
        AccountEntity sourceAccount = executorHelper.findSourceAccount(transfer.getSource(), ownAccounts);

        // In the ICA-banken app a user can change amount and due date of the e-invoice. If the change occurs after
        // a refresh we might have the wrong data for those fields when displaying the e-invoice to the user.
        //
        // We check if the e-invoice have been modified, if so the user must refresh their credentials so that we
        // display the latest version of the e-invoice. This is so that the user doesn't approve something else than
        // what is shown in the Tink app.

        Transfer originalTransfer = getOriginalTransfer(transfer);
        EInvoiceEntity eInvoiceAtBank = findEInvoiceAtBank(invoiceId, originalTransfer);
        Transfer transferAtBank = eInvoiceAtBank.toTinkTransfer(catalog);

        // Important validateEInvoiceOnOurSide() is called _before_ validateEInvoiceOnTheirSide(...) since the latter
        // might auto-correct due date (or other things). If in wrong order, due date validation on our side will fail.
        validateEInvoiceOnOurSide(transfer, transferAtBank);
        updateIfNecessary(transfer, transferAtBank, invoiceId);
        validateEInvoiceOnTheirSide(sourceAccount, invoiceId);

        if (!eInvoiceAtBank.isRecipientConfirmed()) {
            signEInvoice(invoiceId);
        }

        acceptEInvoice(sourceAccount.getAccountId(), invoiceId);
    }

    private void acceptEInvoice(String accountId, String invoiceId) {
        AcceptEInvoiceTransferRequest request = AcceptEInvoiceTransferRequest.create(accountId, invoiceId);
        apiClient.acceptEInvoice(request);
    }

    private void signEInvoice(String invoiceId) {
        String reference = executorHelper.signEInvoice(invoiceId);

        finishEInvoiceSign(reference, invoiceId);
    }

    private void finishEInvoiceSign(String requestId, String invoiceId) {
        FinishEInvoiceSignRequest request = FinishEInvoiceSignRequest.create(requestId, invoiceId);
        apiClient.finishEInvoiceSign(request);
    }

    private void updateIfNecessary(final Transfer potentiallyModifiedTransfer, final Transfer transferAtBank,
            String invoiceId) {

        boolean shouldUpdate = false;
        Amount amountToSet = transferAtBank.getAmount();
        Date dueDateToSet = transferAtBank.getDueDate();

        // Amount
        if (!transferAtBank.getAmount().equals(potentiallyModifiedTransfer.getAmount())) {
            amountToSet = potentiallyModifiedTransfer.getAmount();
            shouldUpdate = true;
        }

        // Due date
        if (!transferAtBank.getDueDate().equals(potentiallyModifiedTransfer.getDueDate())) {
            dueDateToSet = potentiallyModifiedTransfer.getDueDate();
            shouldUpdate = true;
        }

        if (shouldUpdate) {
            updateEInvoice(dueDateToSet, amountToSet, invoiceId);
        }
    }

    private void updateEInvoice(Date dueDate, Amount amount, String invoiceId) {
        UpdateEInvoiceRequest updateEInvoiceRequest = UpdateEInvoiceRequest.create(dueDate, amount, invoiceId);

        ValidateEInvoiceResponse response = apiClient.updateEInvoice(updateEInvoiceRequest);

        if (response.isValidationError()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage(String.format("%s: %s", IcaBankenConstants.LogMessage.EINVOICE_UPDATE_ERROR, response))
                    .setEndUserMessage(TransferExecutionException.EndUserMessage.PAYMENT_UPDATE_FAILED)
                    .build();
        }
    }

    private void validateEInvoiceOnOurSide(final Transfer potentiallyModifiedTransfer,
            final Transfer bankTransfer) {

        // Destination account.
        validateFieldsMatch(bankTransfer.getDestination(), potentiallyModifiedTransfer.getDestination(),
                IcaBankenConstants.LogMessage.EINVOICE_DESTINATION_MODIFIED,
                TransferExecutionException.EndUserMessage.EINVOICE_MODIFY_DESTINATION);

        // Type
        Preconditions.checkState(bankTransfer.getType().equals(potentiallyModifiedTransfer.getType()));

        // Source message
        validateFieldsMatch(bankTransfer.getSourceMessage(), potentiallyModifiedTransfer.getSourceMessage(),
                IcaBankenConstants.LogMessage.EINVOICE_SRC_MSG_MODIFIED,
                TransferExecutionException.EndUserMessage.EINVOICE_MODIFY_SOURCE_MESSAGE);

        // Destination message
        validateFieldsMatch(bankTransfer.getDestinationMessage(), potentiallyModifiedTransfer.getDestinationMessage(),
                IcaBankenConstants.LogMessage.EINVOICE_DEST_MSG_MODIFIED,
                TransferExecutionException.EndUserMessage.EINVOICE_MODIFY_DESTINATION_MESSAGE);
    }

    private void validateFieldsMatch(Object oldField, Object potentiallyModifiedField, String internalMessage,
            TransferExecutionException.EndUserMessage endUserMessage) {
        if (!Objects.equal(oldField, potentiallyModifiedField)) {
            throw TransferExecutionException
                    .builder(SignableOperationStatuses.CANCELLED)
                    .setMessage(String.format("%s Old: %s Current: %s",
                            internalMessage, potentiallyModifiedField, potentiallyModifiedField))
                    .setEndUserMessage(catalog.getString(endUserMessage))
                    .build();
        }
    }

    private void validateEInvoiceOnTheirSide(final AccountEntity account, final String invoiceId) {
        ValidateEInvoiceResponse validateResponse = null;

        try {
            validateResponse = WHILE_ICABANKEN_CORRECTS_VALIDATION_ERROR_RETRYER
                    .call(() -> validateEInvoice(account.getAccountId(), invoiceId));
        } catch (ExecutionException | RetryException e) {
            log.warn("Caught exception when trying to validate e-invoice", e);
        }

        if (validateResponse == null || validateResponse.isValidationError()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage(String.format("%s: %s", IcaBankenConstants.LogMessage.EINVOICE_VALIDATE_ERROR,
                            validateResponse))
                    .setEndUserMessage(TransferExecutionException.EndUserMessage.EINVOICE_VALIDATE_FAILED)
                    .build();
        }
    }

    private ValidateEInvoiceResponse validateEInvoice(final String accountId, final String eInvoiceId) {
        // Might return 409 - Angivet datum har ändrats till närmast möjliga dag. See
        // WHILE_ICABANKEN_CORRECTS_VALIDATION_ERROR_RETRYER.
        ValidateEInvoiceRequest validateRequest = ValidateEInvoiceRequest.create(accountId, eInvoiceId);
        return apiClient.validateEInvoice(validateRequest);
    }

    private String getInvoiceIdFrom(Transfer transfer) {
        final Optional<String> invoiceId = transfer.getPayloadValue(TransferPayloadType.PROVIDER_UNIQUE_ID);

        if (!invoiceId.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage(IcaBankenConstants.LogMessage.PROVIDER_UNIQUE_ID_NOT_FOUND)
                    .setEndUserMessage(catalog.getString(
                            TransferExecutionException.EndUserMessage.EINVOICE_NO_MATCHES))
                    .build();
        }

        return invoiceId.get();
    }

    private Transfer getOriginalTransfer(Transfer transfer) {
        return transfer.getOriginalTransfer().orElseThrow(
                () -> TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                        .setMessage(IcaBankenConstants.LogMessage.NO_ORIGINAL_TRANSFER)
                        .setEndUserMessage(
                                catalog.getString(TransferExecutionException.EndUserMessage.EINVOICE_VALIDATE_FAILED))
                        .build());
    }

    private EInvoiceEntity findEInvoiceAtBank(String invoiceId, Transfer originalTransfer) {
        EInvoiceEntity eInvoice = findEInvoice(invoiceId);
        Transfer transferAtBank = eInvoice.toTinkTransfer(catalog);

        if (!originalTransfer.getHash().equalsIgnoreCase(transferAtBank.getHash())) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setMessage(IcaBankenConstants.LogMessage.EINVOICE_MODIFIED_IN_BANK_APP)
                    .setEndUserMessage(
                            IcaBankenConstants.UserMessage.EINVOICE_MODIFIED_IN_BANK_APP.getKey().get()).build();
        }

        return eInvoice;
    }

    private EInvoiceEntity findEInvoice(String invoiceId) {
        EInvoiceBodyEntity eInvoiceBodyEntity = apiClient.fetchEInvoices();
        Optional<EInvoiceEntity> invoiceEntity = eInvoiceBodyEntity.getInvoiceById(invoiceId);

        if (!invoiceEntity.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage(IcaBankenConstants.LogMessage.EINVOICE_NOT_FOUND)
                    .setEndUserMessage(
                            catalog.getString(TransferExecutionException.EndUserMessage.EINVOICE_NO_MATCHES))
                    .build();
        }

        return invoiceEntity.get();
    }
}
