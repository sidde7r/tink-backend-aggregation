package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.BankIdPolling;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.TransferExceptionMessage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.entities.PaymentSourceAccount;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.rpc.PaymentInitSignResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.rpc.PaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.upcomingtransaction.entities.UpcomingPaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.upcomingtransaction.rpc.FetchPaymentsResponse;
import se.tink.backend.aggregation.nxgen.controllers.transfer.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.signableoperation.enums.InternalStatus;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.rpc.Transfer;

@AllArgsConstructor
public class SkandiaBankenPaymentExecutor implements PaymentExecutor {

    SkandiaBankenApiClient apiClient;
    SupplementalInformationController supplementalInformationController;

    @Override
    public void executePayment(Transfer transfer) throws TransferExecutionException {

        PaymentSourceAccount sourceAccount = getPaymentSourceAccount(transfer);
        PaymentRequest paymentRequest =
                PaymentRequest.createPaymentRequest(transfer, sourceAccount);
        submitPayment(paymentRequest);
        String encryptedPaymentId = getEncryptedPaymentIdFromBank(paymentRequest);

        try {
            signPayment(encryptedPaymentId);
        } catch (TransferExecutionException e) {
            deleteUnapprovedPayment(encryptedPaymentId);
            throw e;
        }
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
            throw getTransferFailedException(
                    TransferExceptionMessage.SUBMIT_PAYMENT_FAILED,
                    EndUserMessage.TRANSFER_EXECUTE_FAILED,
                    InternalStatus.BANK_ERROR_CODE_NOT_HANDLED_YET);
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

    private String getEncryptedPaymentIdFromBank(PaymentRequest paymentRequest) {
        FetchPaymentsResponse unapprovedPayments = apiClient.fetchUnapprovedPayments();
        return findPayment(unapprovedPayments, paymentRequest).getEncryptedPaymentId();
    }

    private UpcomingPaymentEntity findPayment(
            FetchPaymentsResponse unapprovedPayments, PaymentRequest paymentRequest) {
        return unapprovedPayments.stream()
                .filter(paymentEntity -> paymentEntity.isSamePayment(paymentRequest))
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
