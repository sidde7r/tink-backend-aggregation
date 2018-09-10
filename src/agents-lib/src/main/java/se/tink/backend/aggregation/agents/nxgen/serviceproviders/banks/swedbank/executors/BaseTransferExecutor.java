package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors;

import com.google.api.client.http.HttpStatusCodes;
import java.util.List;
import java.util.Optional;
import tink.org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc.ConfirmTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc.InitiateSignTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc.RegisteredTransfersResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc.TransferTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinksEntity;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.core.transfer.SignableOperationStatuses;

public class BaseTransferExecutor {
    private static final Logger log = LoggerFactory.getLogger(BaseTransferExecutor.class);

    protected final String EMPTY_STRING = "";

    protected final SwedbankDefaultApiClient apiClient;
    protected final SwedbankTransferHelper transferHelper;

    protected BaseTransferExecutor(SwedbankDefaultApiClient apiClient, SwedbankTransferHelper transferHelper) {
        this.apiClient = apiClient;
        this.transferHelper = transferHelper;
    }

    protected void signAndConfirmTransfer(RegisteredTransfersResponse registeredTransfersResponse) {
        LinksEntity links = registeredTransfersResponse.getLinks();
        ConfirmTransferResponse confirmTransferResponse = null;

        try {

            Optional<LinkEntity> confirmTransferLink = Optional.ofNullable(links.getNext());

            // Sign the transfer if needed.
            if (!confirmTransferLink.isPresent()) {
                InitiateSignTransferResponse initiateSignTransfer = apiClient.signExternalTransfer(links.getSignOrThrow());
                links = transferHelper.collectBankId(initiateSignTransfer);

                confirmTransferLink = Optional.ofNullable(links.getNext());

                // Prepare for remove of the transfers if the signing failed.
                if (!confirmTransferLink.isPresent()) {
                    registeredTransfersResponse = apiClient.registeredTransfers();

                    throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                            .setMessage("No confirm transfer link found. Transfer failed.")
                            .setEndUserMessage(TransferExecutionException.EndUserMessage.TRANSFER_CONFIRM_FAILED)
                            .setMessage(SwedbankBaseConstants.ErrorMessage.TRANSFER_CONFIRM_FAILED)
                            .build();
                }
            }

            // Confirm the transfer.
            SwedbankTransferHelper.ensureLinksNotNull(links,
                    TransferExecutionException.EndUserMessage.TRANSFER_CONFIRM_FAILED,
                    SwedbankBaseConstants.ErrorMessage.TRANSFER_CONFIRM_FAILED);

            confirmTransferResponse = apiClient.confirmTransfer(links.getNextOrThrow());
            SwedbankTransferHelper.confirmSuccessfulTransfer(confirmTransferResponse,
                    registeredTransfersResponse.getIdToConfirm().orElse(EMPTY_STRING));
        } catch (Exception e) {
            if (confirmTransferResponse != null && !confirmTransferResponse.getRejectedTransactions().isEmpty()) {
                deleteTransfers(confirmTransferResponse.getRejectedTransactions());
            } else {
                deleteTransfers(registeredTransfersResponse.getRegisteredTransactions());
            }
            throw e;
        }
    }

    // convert HttpResponseException to TransferExecutionException if response indicates bad date for payment
    // used by EInvoice and Payment
    protected RuntimeException convertExceptionIfBadPaymentDate(HttpResponseException hre) throws TransferExecutionException {
        HttpResponse httpResponse = hre.getResponse();
        // swedbank doesn't allow payment with due date today
        if (httpResponse.getStatus() == HttpStatus.SC_BAD_REQUEST) {
            ErrorResponse errorResponse = httpResponse.getBody(ErrorResponse.class);

            if (errorResponse.hasErrorField(SwedbankBaseConstants.ErrorField.DATE)) {
                return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                        .setEndUserMessage(TransferExecutionException.EndUserMessage.INVALID_DUEDATE_TOO_SOON_OR_NOT_BUSINESSDAY)
                        .setMessage(SwedbankBaseConstants.ErrorMessage.TRANSFER_REGISTER_FAILED).build();
            }
        }

        return hre;
    }

    /**
     * Delete a set of transfer groups (used when cancelling injected transfers).
     */
    private void deleteTransfers(List<TransferTransactionEntity> transferTransactions) {
        try {
            for (TransferTransactionEntity transferTransaction : transferTransactions) {
                for (TransactionEntity transactionEntity : transferTransaction.getTransactions()) {
                    deleteTransfer(transactionEntity);
                }
            }
        } catch (Exception deleteException) {
            log.warn("Could not delete transfers in outbox. "
                    + "If unsigned transfers are left here, user could end up in a deadlock.", deleteException);
        }
    }

    private void deleteTransfer(TransactionEntity transaction) {
        HttpResponse deleteResponse = apiClient.deleteTransfer(transaction.getLinks().getDelete());

        if (deleteResponse.getStatus() != HttpStatusCodes.STATUS_CODE_OK) {
            ErrorResponse errorResponse = deleteResponse.getBody(ErrorResponse.class);
            String errorMessages = errorResponse.getAllErrors();
            log.warn(String.format("#Swedbank-v5 - Delete transfer - Error messages: %s", errorMessages));
        }
    }
}
