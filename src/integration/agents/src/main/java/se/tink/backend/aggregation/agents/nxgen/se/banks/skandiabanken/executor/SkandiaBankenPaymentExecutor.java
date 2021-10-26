package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.BankIdPolling;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.PaymentTransfer;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.TransferExceptionMessage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.entities.PaymentSourceAccount;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.rpc.PaymentInitSignResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.rpc.PaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.utils.SkandiaBankenDateUtils;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.utils.SkandiaBankenExecutorUtils;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.upcomingtransaction.entities.UpcomingPaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.upcomingtransaction.rpc.FetchPaymentsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.transfer.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.signableoperation.enums.InternalStatus;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

@RequiredArgsConstructor
public class SkandiaBankenPaymentExecutor implements PaymentExecutor {

    final SkandiaBankenApiClient apiClient;
    final SupplementalInformationController supplementalInformationController;

    @Override
    public void executePayment(Transfer transfer) throws TransferExecutionException {

        validateTransferOrThrow(transfer);

        PaymentSourceAccount sourceAccount = getPaymentSourceAccount(transfer);

        Date paymentDate = getPaymentDate(transfer);

        // Skandia can respond with 500 on bad input. Removing the filter that handles 500
        // responses to not mistake it for bank side failures.
        apiClient.removeBankServiceInternalErrorFilter();

        PaymentRequest paymentRequest =
                PaymentRequest.createPaymentRequest(transfer, paymentDate, sourceAccount);
        submitPayment(paymentRequest);
        String encryptedPaymentId = getEncryptedPaymentIdFromBank(paymentRequest, paymentDate);

        try {
            signPayment(encryptedPaymentId);
        } catch (TransferExecutionException e) {
            deleteUnapprovedPayment(encryptedPaymentId);
            throw e;
        }

        // Re-add filter after PIS flow, in case there's a refresh after we would want to
        // handle 500 responses as bank side failures.
        apiClient.addBankServiceInternalErrorFilter();
    }

    private void validateTransferOrThrow(Transfer transfer) {
        throwIfNotBgOrPgPayment(transfer);
        throwIfAmountIsLessThanMinAmount(transfer);
        throwIfUnstructuredRefLongerThanMax(transfer);
    }

    private void throwIfNotBgOrPgPayment(Transfer transfer) {
        if (!transfer.getDestination().isGiroIdentifier()) {
            throw getTransferCancelledException(
                    TransferExceptionMessage.INVALID_PAYMENT_TYPE,
                    EndUserMessage.END_USER_WRONG_PAYMENT_TYPE,
                    InternalStatus.INVALID_PAYMENT_TYPE);
        }
    }

    private void throwIfAmountIsLessThanMinAmount(Transfer transfer) {
        if (transfer.getAmount().getValue() < PaymentTransfer.MIN_AMOUNT) {
            throw getTransferCancelledException(
                    TransferExceptionMessage.INVALID_MINIMUM_AMOUNT,
                    EndUserMessage.INVALID_MINIMUM_AMOUNT,
                    InternalStatus.INVALID_MINIMUM_AMOUNT);
        }
    }

    private void throwIfUnstructuredRefLongerThanMax(Transfer transfer) {
        if (isUnstructuredRefAndLongerThanMax(transfer.getRemittanceInformation())) {
            throw getTransferCancelledException(
                    TransferExceptionMessage.INVALID_UNSTRUCTURED_LENGTH,
                    EndUserMessage.INVALID_DESTINATION_MESSAGE,
                    InternalStatus.INVALID_DESTINATION_MESSAGE);
        }
    }

    private boolean isUnstructuredRefAndLongerThanMax(RemittanceInformation remittanceInformation) {
        return remittanceInformation.isOfType(RemittanceInformationType.UNSTRUCTURED)
                && remittanceInformation.getValue().length()
                        > PaymentTransfer.UNSTRUCTURED_MAX_LENGTH;
    }

    private Date getPaymentDate(Transfer transfer) {
        SkandiaBankenDateUtils dateUtils = new SkandiaBankenDateUtils();
        return dateUtils.getTransferDateForBgPg(transfer.getDueDate());
    }

    private PaymentSourceAccount getPaymentSourceAccount(Transfer transfer) {
        Collection<PaymentSourceAccount> paymentSourceAccounts =
                apiClient.fetchPaymentSourceAccounts();

        return SkandiaBankenExecutorUtils.tryFindOwnAccount(
                        transfer.getSource(), paymentSourceAccounts)
                .orElseThrow(
                        () ->
                                getTransferCancelledException(
                                        TransferExceptionMessage.SOURCE_NOT_FOUND,
                                        EndUserMessage.SOURCE_NOT_FOUND,
                                        InternalStatus.INVALID_SOURCE_ACCOUNT));
    }

    private void submitPayment(PaymentRequest paymentRequest) {
        try {
            apiClient.submitPayment(paymentRequest);
        } catch (HttpResponseException e) {
            throwIfInvalidDateError(e.getResponse());
            throwIfInvalidOcrError(e.getResponse());

            throw getTransferFailedException(
                    TransferExceptionMessage.SUBMIT_PAYMENT_FAILED,
                    EndUserMessage.TRANSFER_EXECUTE_FAILED,
                    InternalStatus.BANK_ERROR_CODE_NOT_HANDLED_YET);
        }
    }

    private void throwIfInvalidDateError(HttpResponse response) {

        if (response.getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR
                && MediaType.APPLICATION_JSON_TYPE.isCompatible(response.getType())) {
            ErrorResponse errorResponse = response.getBody(ErrorResponse.class);

            if (errorResponse.isInvalidPaymentDate()) {
                throw getTransferCancelledException(
                        TransferExceptionMessage.INVALID_PAYMENT_DATE,
                        EndUserMessage.INVALID_DUEDATE_TOO_SOON_OR_NOT_BUSINESSDAY,
                        InternalStatus.INVALID_DUE_DATE);
            }
        }
    }

    private void throwIfInvalidOcrError(HttpResponse response) {

        if (response.getStatus() == HttpStatus.SC_BAD_REQUEST
                && MediaType.APPLICATION_JSON_TYPE.isCompatible(response.getType())) {
            ErrorResponse errorResponse = response.getBody(ErrorResponse.class);

            if (errorResponse.isInvalidOcrError()) {
                throw getTransferCancelledException(
                        TransferExceptionMessage.INVALID_OCR,
                        EndUserMessage.INVALID_OCR,
                        InternalStatus.INVALID_DESTINATION_MESSAGE);
            }
        }
    }

    private void signPayment(String encryptedPaymentId) {
        String signReference = initPaymentSigning(encryptedPaymentId);

        supplementalInformationController.openMobileBankIdAsync(null);

        poll(signReference);

        try {
            apiClient.completePayment(encryptedPaymentId, signReference);
        } catch (HttpResponseException e) {
            throw getTransferFailedException(
                    TransferExceptionMessage.COMPLETE_PAYMENT_FAILED,
                    EndUserMessage.TRANSFER_CONFIRM_FAILED,
                    InternalStatus.BANK_ERROR_CODE_NOT_HANDLED_YET);
        }
    }

    private String initPaymentSigning(String encryptedPaymentId) {
        PaymentInitSignResponse initSignResponse;
        try {
            initSignResponse = apiClient.initSignPayment(encryptedPaymentId);
        } catch (HttpResponseException e) {
            throw getTransferFailedException(
                    TransferExceptionMessage.INIT_SIGN_FAILED,
                    EndUserMessage.SIGN_TRANSFER_FAILED,
                    InternalStatus.BANK_ERROR_CODE_NOT_HANDLED_YET);
        }

        return initSignResponse
                .getSignReference()
                .orElseThrow(
                        () ->
                                getTransferFailedException(
                                        TransferExceptionMessage.NO_SIGN_REFERENCE,
                                        EndUserMessage.SIGN_TRANSFER_FAILED,
                                        InternalStatus.BANK_ERROR_CODE_NOT_HANDLED_YET));
    }

    private String getEncryptedPaymentIdFromBank(PaymentRequest paymentRequest, Date paymentDate) {
        FetchPaymentsResponse unapprovedPayments = apiClient.fetchUnapprovedPayments();
        return findPayment(unapprovedPayments, paymentRequest, paymentDate).getEncryptedPaymentId();
    }

    private UpcomingPaymentEntity findPayment(
            FetchPaymentsResponse unapprovedPayments,
            PaymentRequest paymentRequest,
            Date paymentDate) {
        return unapprovedPayments.stream()
                .filter(paymentEntity -> paymentEntity.isSamePayment(paymentRequest, paymentDate))
                .findFirst()
                .orElseThrow(
                        () ->
                                getTransferFailedException(
                                        TransferExceptionMessage.PAYMENT_NOT_FOUND,
                                        EndUserMessage.TRANSFER_EXECUTE_FAILED,
                                        InternalStatus.BANK_ERROR_CODE_NOT_HANDLED_YET));
    }

    public void poll(String signReference) {
        BankIdStatus status;

        Uninterruptibles.sleepUninterruptibly(BankIdPolling.INITIAL_SLEEP, TimeUnit.MILLISECONDS);
        for (int i = 0; i < BankIdPolling.MAX_ATTEMPTS; i++) {
            status = getPaymentSignStatus(signReference);

            switch (status) {
                case DONE:
                    return;
                case WAITING:
                    break;
                case CANCELLED:
                    throw getTransferCancelledException(
                            TransferExceptionMessage.SIGN_CANCELLED,
                            EndUserMessage.BANKID_CANCELLED,
                            InternalStatus.BANKID_CANCELLED);
                default:
                    throw getTransferFailedException(
                            TransferExceptionMessage.UNKNOWN_SIGN_STATUS,
                            EndUserMessage.BANKID_TRANSFER_FAILED,
                            InternalStatus.BANKID_UNKNOWN_EXCEPTION);
            }

            Uninterruptibles.sleepUninterruptibly(
                    BankIdPolling.SLEEP_BETWEEN_POLLS, TimeUnit.MILLISECONDS);
        }

        throw getTransferCancelledException(
                TransferExceptionMessage.SIGN_TIMEOUT,
                EndUserMessage.BANKID_NO_RESPONSE,
                InternalStatus.BANKID_NO_RESPONSE);
    }

    private BankIdStatus getPaymentSignStatus(String signReference) {
        try {
            return apiClient.pollPaymentSignStatus(signReference);
        } catch (HttpResponseException e) {
            throw getTransferFailedException(
                    TransferExceptionMessage.POLL_SIGN_STATUS_FAILED,
                    EndUserMessage.BANKID_TRANSFER_FAILED,
                    InternalStatus.BANKID_UNKNOWN_EXCEPTION);
        }
    }

    private void deleteUnapprovedPayment(String encryptedPaymentId) {
        try {
            apiClient.deleteUnapprovedPayment(encryptedPaymentId);
        } catch (HttpResponseException e) {
            throw getTransferFailedException(
                    TransferExceptionMessage.PAYMENT_DELETE_FAILED,
                    EndUserMessage.SIGN_AND_REMOVAL_FAILED,
                    InternalStatus.BANK_ERROR_CODE_NOT_HANDLED_YET);
        }
    }

    private TransferExecutionException getTransferCancelledException(
            String message, EndUserMessage endUserMessage, InternalStatus internalStatus) {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(message)
                .setEndUserMessage(endUserMessage)
                .setInternalStatus(internalStatus.toString())
                .build();
    }

    private TransferExecutionException getTransferFailedException(
            String message, EndUserMessage endUserMessage, InternalStatus internalStatus) {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(message)
                .setEndUserMessage(endUserMessage)
                .setInternalStatus(internalStatus.toString())
                .build();
    }
}
