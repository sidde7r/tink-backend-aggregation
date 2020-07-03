package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors;

import com.google.api.client.http.HttpStatusCodes;
import java.util.List;
import java.util.Optional;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc.ConfirmTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc.InitiateSignTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc.RegisteredTransfersResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc.TransferTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.updatepayment.rpc.PaymentDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.utilities.SwedbankDateUtils;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.transferdestination.rpc.PaymentBaseinfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.transferdestination.rpc.TransferDestinationAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.BankProfile;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinksEntity;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.rpc.Transfer;

public class BaseTransferExecutor {
    private static final Logger log = LoggerFactory.getLogger(BaseTransferExecutor.class);

    protected final String EMPTY_STRING = "";

    protected final SwedbankDefaultApiClient apiClient;
    protected final SwedbankTransferHelper transferHelper;
    protected final SwedbankDateUtils dateUtils;

    protected BaseTransferExecutor(
            SwedbankDefaultApiClient apiClient,
            SwedbankTransferHelper transferHelper,
            SwedbankDateUtils dateUtils) {
        this.apiClient = apiClient;
        this.transferHelper = transferHelper;
        this.dateUtils = dateUtils;
    }

    /**
     * This method goes through all of the users profiles in order to find the source account of the
     * transfer. If no source account is found an exception is thrown, otherwise the source account
     * will be returned and the profile that the account belongs to will be selected.
     */
    protected String getSourceAccountIdAndSelectProfile(Transfer transfer) {

        for (BankProfile bankProfile : apiClient.getBankProfiles()) {

            PaymentBaseinfoResponse paymentBaseInfo = bankProfile.getPaymentBaseinfoResponse();

            AccountIdentifier transferSourceAccount =
                    SwedbankTransferHelper.getSourceAccount(transfer);

            Optional<TransferDestinationAccountEntity> transferDestinationAccountEntity =
                    paymentBaseInfo.getSourceAccount(transferSourceAccount);

            if (transferDestinationAccountEntity.isPresent()) {
                String sourceAccountId =
                        paymentBaseInfo.validateAndGetSourceAccountId(
                                transferDestinationAccountEntity.get());
                apiClient.selectProfile(bankProfile);

                return sourceAccountId;
            }
        }

        throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setEndUserMessage(TransferExecutionException.EndUserMessage.SOURCE_NOT_FOUND)
                .setMessage(SwedbankBaseConstants.ErrorMessage.SOURCE_NOT_FOUND)
                .build();
    }

    protected void signAndConfirmTransfer(RegisteredTransfersResponse registeredTransfersResponse) {
        LinksEntity links = registeredTransfersResponse.getLinks();
        ConfirmTransferResponse confirmTransferResponse = null;

        try {

            Optional<LinkEntity> confirmTransferLink = Optional.ofNullable(links.getNext());

            // Sign the transfer if needed.
            if (!confirmTransferLink.isPresent()) {
                if (transferHelper.isBankId()) {
                    InitiateSignTransferResponse initiateSignTransfer =
                            apiClient.signExternalTransferBankId(links.getSignOrThrow());
                    links = transferHelper.collectBankId(initiateSignTransfer);
                } else {
                    links = transferHelper.tokenSignTransfer(links);
                }

                confirmTransferLink = Optional.ofNullable(links.getNext());

                // Prepare for remove of the transfers if the signing failed.
                if (!confirmTransferLink.isPresent()) {
                    registeredTransfersResponse = apiClient.registeredTransfers();

                    throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                            .setMessage("No confirm transfer link found. Transfer failed.")
                            .setEndUserMessage(
                                    TransferExecutionException.EndUserMessage
                                            .TRANSFER_CONFIRM_FAILED)
                            .setMessage(SwedbankBaseConstants.ErrorMessage.TRANSFER_CONFIRM_FAILED)
                            .build();
                }
            }

            // Confirm the transfer.
            SwedbankTransferHelper.ensureLinksNotNull(
                    links,
                    TransferExecutionException.EndUserMessage.TRANSFER_CONFIRM_FAILED,
                    SwedbankBaseConstants.ErrorMessage.TRANSFER_CONFIRM_FAILED);

            confirmTransferResponse = apiClient.confirmTransfer(links.getNextOrThrow());
            transferHelper.confirmSuccessfulTransferOrThrow(
                    confirmTransferResponse,
                    registeredTransfersResponse.getIdToConfirm().orElse(EMPTY_STRING));
        } catch (Exception e) {
            if (confirmTransferResponse != null
                    && !confirmTransferResponse.getRejectedTransactions().isEmpty()) {
                deleteTransfers(confirmTransferResponse.getRejectedTransactions());
            } else {
                deleteTransfers(registeredTransfersResponse.getRegisteredTransactions());
            }
            throw e;
        }
    }

    // convert HttpResponseException to TransferExecutionException if response indicates bad date
    // for payment or transfer
    protected RuntimeException convertExceptionIfBadPayment(HttpResponseException hre)
            throws TransferExecutionException {
        HttpResponse httpResponse = hre.getResponse();
        // swedbank doesn't allow payment with due date today
        if (httpResponse.getStatus() == HttpStatus.SC_BAD_REQUEST) {
            ErrorResponse errorResponse = httpResponse.getBody(ErrorResponse.class);

            if (errorResponse.hasErrorField(SwedbankBaseConstants.ErrorField.DATE)) {
                return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                        .setEndUserMessage(
                                TransferExecutionException.EndUserMessage
                                        .INVALID_DUEDATE_TOO_SOON_OR_NOT_BUSINESSDAY)
                        .setMessage(SwedbankBaseConstants.ErrorMessage.TRANSFER_REGISTER_FAILED)
                        .setException(hre)
                        .build();
            } else if (errorResponse.hasErrorField(SwedbankBaseConstants.ErrorField.REFERENCE)) {
                return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                        .setEndUserMessage(TransferExecutionException.EndUserMessage.INVALID_OCR)
                        .setMessage(SwedbankBaseConstants.ErrorMessage.TRANSFER_REGISTER_FAILED)
                        .setException(hre)
                        .build();
            }
        }

        return hre;
    }

    /** Delete a set of transfer groups (used when cancelling injected transfers). */
    public void deleteTransfers(List<TransferTransactionEntity> transferTransactions) {
        try {
            for (TransferTransactionEntity transferTransaction : transferTransactions) {
                for (TransactionEntity transactionEntity : transferTransaction.getTransactions()) {
                    deleteTransfer(transactionEntity);
                }
            }
        } catch (Exception deleteException) {
            log.warn(
                    "Could not delete transfers in outbox. "
                            + "If unsigned transfers are left here, user could end up in a deadlock.",
                    deleteException);
        }
    }

    private LinkEntity getDeleteLink(TransactionEntity transaction) {
        final LinkEntity deleteLink = transaction.getLinks().getDelete();
        if (deleteLink != null) {
            return deleteLink;
        }

        // fetch delete link from payment details
        final PaymentDetailsResponse paymentDetailsResponse =
                apiClient.paymentDetails(transaction.getLinks().getSelf());
        return paymentDetailsResponse.getTransaction().getLinks().getDelete();
    }

    private void deleteTransfer(TransactionEntity transaction) {
        HttpResponse deleteResponse = apiClient.deleteTransfer(getDeleteLink(transaction));

        if (deleteResponse.getStatus() != HttpStatusCodes.STATUS_CODE_OK) {
            ErrorResponse errorResponse = deleteResponse.getBody(ErrorResponse.class);
            String errorMessages = errorResponse.getAllErrors();
            log.warn(
                    String.format(
                            "#Swedbank-v5 - Delete transfer - Error messages: %s", errorMessages));
        }
    }
}
