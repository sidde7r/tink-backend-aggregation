package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.google.api.client.repackaged.com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.Accounts.entities.OwnAccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice.entities.EInvoiceEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice.entities.UpdateEInvoiceRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice.entities.ValidateEInvoiceRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice.entities.ValidateEInvoiceResponse;
import se.tink.backend.aggregation.nxgen.controllers.transfer.ApproveEInvoiceExecutor;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.backend.core.Amount;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.i18n.Catalog;

public class IcaBankenApproveEInvoiceExecutor implements ApproveEInvoiceExecutor {

    private final IcaBankenApiClient apiClient;
    //private final AgentContext context;
    //private final TransferMessageFormatter transferMessageFormatter;
    public String invoiceId;
    public String accountId;
    private IcaBankenEInvoiceFetcher eInvoiceFetcher;
    private static final Retryer<ValidateEInvoiceResponse> WHILE_ICABANKEN_CORRECTS_VALIDATION_ERROR_RETRYER = RetryerBuilder.<ValidateEInvoiceResponse>newBuilder()
            .retryIfResult(ValidateEInvoiceResponse::isInvalidButICABankenCorrectedIt)
            // Upper retry bound is important to avoid infinite loop.
            .withStopStrategy(StopStrategies.stopAfterAttempt(3))
            .build();

    public IcaBankenApproveEInvoiceExecutor(IcaBankenApiClient apiClient, AgentContext context, Catalog catalog,
            TransferMessageFormatter transferMessageFormatter, IcaBankenEInvoiceFetcher eInvoiceFetcher) {
        this.apiClient = apiClient;
        //this.context = context;
        //this.transferMessageFormatter = transferMessageFormatter;
        this.eInvoiceFetcher = eInvoiceFetcher;
    }

    @Override
    public void approveEInvoice(final Transfer transfer) throws TransferExecutionException {
        //validateNoUnsignedTransfers();

        //Collection<Transfer> transfers = eInvoiceFetcher.fetchEInvoices(transfer);

        invoiceId = eInvoiceFetcher.getInvoiceIdFrom(transfer);
        Transfer originalTransfer = eInvoiceFetcher.getOriginalTransfer(transfer);

        // In the ICA-banken app a user can change amount and due date of the e-invoice. If the change occurs after
        // a refresh we might have the wrong data for those fields when displaying the e-invoice to the user.
        //
        // We check if the e-invoice have been modified, if so the user must refresh their credentials so that we
        // display the latest version of the e-invoice. This is so that the user doesn't approve something else than
        // what is shown in the Tink app.

        final OwnAccountsEntity sourceAccount = eInvoiceFetcher.fetchSourceAccountFor(transfer);
        final EInvoiceEntity eInvoice = eInvoiceFetcher.findMatchingEInvoice(invoiceId, originalTransfer);
        final Transfer bankTransfer = eInvoice.toTinkTransfer(eInvoiceFetcher.catalog);

        // Important validateEInvoiceOnOurSide() is called _before_ validateEInvoiceOnTheirSide(...) since the latter
        // might auto-correct due date (or other things). If in wrong order, due date validation on our side will fail.
        validateEInvoiceOnOurSide(transfer, bankTransfer);
        updateIfNecessary(transfer, bankTransfer, invoiceId);
        try {
            validateEInvoiceOnTheirSide(sourceAccount, invoiceId);
        } catch (ExecutionException | RetryException e) {
            e.printStackTrace();
        }

        if (!eInvoice.isRecipientConfirmed()) {
            accountId = sourceAccount.getAccountId();
            return;
        }

        apiClient.acceptEInvoiceTransfer(sourceAccount.getAccountId(), invoiceId);
    }

    private void validateEInvoiceOnOurSide(final Transfer potentiallyModifiedTransfer, final Transfer bankTransfer) {

        // Destination account.
        validateFieldsMatch(bankTransfer.getDestination(), potentiallyModifiedTransfer.getDestination(),
                TransferExecutionException.EndUserMessage.EINVOICE_MODIFY_DESTINATION,
                "Destination account cannot be changed.");

        // Type
        Preconditions.checkState(Objects.equal(bankTransfer.getType(), potentiallyModifiedTransfer.getType()));

        // Source message
        validateFieldsMatch(bankTransfer.getSourceMessage(), potentiallyModifiedTransfer.getSourceMessage(),
                TransferExecutionException.EndUserMessage.EINVOICE_MODIFY_SOURCE_MESSAGE,
                "Source message cannot be changed.");

        // Destination message
        validateFieldsMatch(bankTransfer.getDestinationMessage(), potentiallyModifiedTransfer.getDestinationMessage(),
                TransferExecutionException.EndUserMessage.EINVOICE_MODIFY_DESTINATION_MESSAGE,
                "Destination message cannot be changed.");
    }

    private void validateFieldsMatch(Object oldField, Object potentiallyModifiedField,
            TransferExecutionException.EndUserMessage endUserMessage, String internalMessage) {
        if (!Objects.equal(oldField, potentiallyModifiedField)) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setEndUserMessage(eInvoiceFetcher.catalog.getString(endUserMessage))
                    .setMessage(String.format("%s Old: %s Current: %s", internalMessage, potentiallyModifiedField,
                            potentiallyModifiedField))
                    .build();
        }
    }

    private void updateIfNecessary(final Transfer potentiallyModifiedTransfer, final Transfer bankTransfer,
            String invoiceId) {
        boolean shouldUpdate = false;
        Amount amountForUpdate = bankTransfer.getAmount();
        Date dueDateForUpdate = bankTransfer.getDueDate();

        // Amount
        if (!Objects.equal(bankTransfer.getAmount(), potentiallyModifiedTransfer.getAmount())) {
            amountForUpdate = potentiallyModifiedTransfer.getAmount();
            shouldUpdate = true;
        }

        // Due date
        if (!Objects.equal(bankTransfer.getDueDate(), potentiallyModifiedTransfer.getDueDate())) {
            dueDateForUpdate = potentiallyModifiedTransfer.getDueDate();
            shouldUpdate = true;
        }

        if (shouldUpdate) {
            updateEInvoice(dueDateForUpdate, amountForUpdate, invoiceId);
        }
    }

    private void updateEInvoice(Date dueDate, Amount amount, String invoiceId) {
        UpdateEInvoiceRequest updateEInvoiceRequest = new UpdateEInvoiceRequest();

        updateEInvoiceRequest.setPayDate(ThreadSafeDateFormat.FORMATTER_DAILY.format(dueDate));
        updateEInvoiceRequest.setFormattedAmount(amount.getValue());
        updateEInvoiceRequest.setInvoiceId(invoiceId);

        ValidateEInvoiceResponse response = apiClient.updateEInvoice(updateEInvoiceRequest);

        if (response.isValidationError()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage(String.format("Could not update invoice: %s", response))
                    .setEndUserMessage(eInvoiceFetcher.getEndUserMessage(response,
                            TransferExecutionException.EndUserMessage.PAYMENT_UPDATE_FAILED))
                    .build();
        }
    }

    private void validateEInvoiceOnTheirSide(final OwnAccountsEntity account, final String invoiceId)
            throws TransferExecutionException, ExecutionException, RetryException {

        ValidateEInvoiceResponse validateResponse = WHILE_ICABANKEN_CORRECTS_VALIDATION_ERROR_RETRYER.call(
                () -> validateEInvoice(account.getAccountId(), invoiceId));

        if (validateResponse.isValidationError()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage(String.format("Could not validate invoice: %s", validateResponse))
                    .setEndUserMessage(eInvoiceFetcher.getEndUserMessage(validateResponse,
                            TransferExecutionException.EndUserMessage.EINVOICE_VALIDATE_FAILED))
                    .build();
        }
    }

    public String initEInvoice(final String invoiceId) {

        return apiClient.initBankIdEInvoice(invoiceId).getBody().getRequestId();
    }

    public void acceptEInvoice(String accountId, String invoiceId) {
        apiClient.acceptEInvoiceTransfer(accountId, invoiceId);
    }

    private ValidateEInvoiceResponse validateEInvoice(final String accountId, final String eInvoiceId) {
        // Might return 409 - Angivet datum har ändrats till närmast möjliga dag. See
        // WHILE_ICABANKEN_CORRECTS_VALIDATION_ERROR_RETRYER.

        ValidateEInvoiceRequest validateRequest = new ValidateEInvoiceRequest();
        validateRequest.setAccountId(accountId);
        validateRequest.setInvoiceId(eInvoiceId);

        return apiClient.validateEInvoice(validateRequest);
    }
}

