package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.payment;

import static se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage.INVALID_DESTINATION_MESSAGE;
import static se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage.INVALID_OCR;
import static se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage.PAYMENT_CREATE_FAILED;
import static se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage.PAYMENT_NO_MATCHES;
import static se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage.PAYMENT_UPDATE_AMOUNT;
import static se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage.PAYMENT_UPDATE_DESTINATION;
import static se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage.PAYMENT_UPDATE_DESTINATION_MESSAGE;
import static se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage.PAYMENT_UPDATE_DUEDATE;
import static se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage.PAYMENT_UPDATE_FAILED;
import static se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage.PAYMENT_UPDATE_NOT_ALLOWED;
import static se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage.PAYMENT_UPDATE_SOURCE;
import static se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage.PAYMENT_UPDATE_SOURCE_MESSAGE;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants.Transfers;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.ExecutorExceptionResolver;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.entities.DetailedPermissions;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.payment.rpc.PaymentSignRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.rpc.UpdatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.ConfirmInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.ConfirmTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.TransferApprovalRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.TransferApprovalResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.TransferSignResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount.rpc.PaymentDetails;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.interfaces.UpdatablePayment;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.rpc.HandelsbankenSEPaymentContext;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.rpc.PaymentRecipient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator;
import se.tink.backend.aggregation.nxgen.controllers.transfer.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.transfer.UpdatePaymentExecutor;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.SwedishSHBInternalIdentifier;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.formatters.DisplayAccountIdentifierFormatter;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.rpc.Transfer;

public class HandelsbankenSEPaymentExecutor implements PaymentExecutor, UpdatePaymentExecutor {

    private final SupplementalRequester supplementalRequester;
    private final Catalog catalog;
    private final HandelsbankenSEApiClient client;
    private final HandelsbankenSessionStorage sessionStorage;
    private final ExecutorExceptionResolver exceptionResolver;

    public HandelsbankenSEPaymentExecutor(
            SupplementalRequester supplementalRequester,
            Catalog catalog,
            HandelsbankenSEApiClient client,
            HandelsbankenSessionStorage sessionStorage,
            ExecutorExceptionResolver exceptionResolver) {
        this.supplementalRequester = supplementalRequester;
        this.catalog = catalog;
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.exceptionResolver = exceptionResolver;
    }

    @Override
    public void executePayment(Transfer transfer) throws TransferExecutionException {
        HandelsbankenSEPaymentContext context =
                sessionStorage
                        .applicationEntryPoint()
                        .map(client::paymentContext)
                        .orElseThrow(() -> paymentFailedException(PAYMENT_CREATE_FAILED));
        verifySourceAccount(transfer.getSource(), context);

        PaymentRecipient paymentRecipient = verifyRecipient(transfer.getDestination(), context);

        if (transfer.getRemittanceInformation().getType() == null) {
            validateDestinationMessage(paymentRecipient, transfer.getRemittanceInformation().getValue());
        }
        signTransfer(context.toCreate(), PaymentSignRequest.create(transfer, paymentRecipient));
    }

    @Override
    public void updatePayment(Transfer transfer) throws TransferExecutionException {
        PaymentDetails paymentDetails =
                fetchPaymentDetails(
                        transfer.getOriginalTransfer()
                                .orElseThrow(
                                        (() -> paymentFailedException(PAYMENT_UPDATE_FAILED))));

        updateIfChanged(transfer, paymentDetails);
    }

    private void verifySourceAccount(
            AccountIdentifier source, HandelsbankenSEPaymentContext context) {
        String sourceNumber =
                (source instanceof SwedishIdentifier
                        ? ((SwedishIdentifier) source).getAccountNumber()
                        : source.getIdentifier());

        context.retrieveOwnedSourceAccounts().stream()
                .filter(
                        generalAccountEntity -> {
                            AccountIdentifier account =
                                    generalAccountEntity.generalGetAccountIdentifier();
                            String accountNumber =
                                    (account instanceof SwedishIdentifier
                                            ? ((SwedishIdentifier) account).getAccountNumber()
                                            : account.getIdentifier());
                            return accountNumber.equals(sourceNumber);
                        })
                .findFirst()
                .orElseThrow(() -> paymentFailedException(PAYMENT_UPDATE_FAILED));
    }

    private PaymentRecipient verifyRecipient(
            AccountIdentifier destination, HandelsbankenSEPaymentContext paymentContext) {
        return paymentContext.paymentRecipients().stream()
                .filter(
                        recipient -> {
                            DefaultAccountIdentifierFormatter defaultFormatter =
                                    new DefaultAccountIdentifierFormatter();
                            return Objects.equals(
                                    recipient.accountIdentifier().getIdentifier(defaultFormatter),
                                    destination.getIdentifier(defaultFormatter));
                        })
                .findFirst()
                .orElse(
                        client.lookupRecipient(
                                paymentContext,
                                destination.getIdentifier(
                                        new DisplayAccountIdentifierFormatter())));
    }

    private void validateDestinationMessage(
            PaymentRecipient destination, String remittanceInformation) {
        GiroMessageValidator.ValidationResult validationResult =
                GiroMessageValidator.create(destination.getOcrCheck().getValidationConfiguration())
                        .validate(remittanceInformation);

        switch (validationResult.getAllowedType()) {
            case OCR:
                validationResult
                        .getValidOcr()
                        .orElseThrow(() -> paymentCanceledException(INVALID_OCR));
                break;
            case MESSAGE:
                validationResult
                        .getValidMessage()
                        .orElseThrow(() -> paymentCanceledException(INVALID_DESTINATION_MESSAGE));
                break;
            default:
                validationResult
                        .getValidOcr()
                        .orElse(
                                validationResult
                                        .getValidMessage()
                                        .orElseThrow(
                                                () ->
                                                        paymentCanceledException(
                                                                INVALID_DESTINATION_MESSAGE)));
        }
    }

    private PaymentDetails fetchPaymentDetails(Transfer originalTransfer) {
        return sessionStorage
                .applicationEntryPoint()
                .map(client::pendingTransactions)
                .orElseThrow((() -> paymentFailedException(PAYMENT_UPDATE_FAILED)))
                .getPendingTransactionStream()
                .map(client::paymentDetails)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(
                        paymentDetails1 ->
                                Objects.equals(
                                        originalTransfer.getHash(),
                                        paymentDetails1.toTransfer().getHash()))
                .findFirst()
                .orElseThrow(() -> paymentFailedException(PAYMENT_NO_MATCHES));
    }

    private void signTransfer(Optional<URL> url, PaymentSignRequest paymentSignRequest) {
        TransferSignResponse transferSignResponse =
                url.map(requestUrl -> client.signTransfer(requestUrl, paymentSignRequest))
                        .orElseThrow(() -> paymentFailedException(PAYMENT_CREATE_FAILED));
        transferSignResponse.validateResponse(exceptionResolver);

        confirmTransfer(transferSignResponse, null);
    }

    // Made public since it's used in HandelsbankenSEEInvoiceExecutor and
    // HandelsbankenSEBankTransferExecutor
    public void confirmTransfer(
            TransferSignResponse transferSignResponse,
            TransferApprovalRequest transferApprovalRequest) {

        ConfirmInfoResponse confirmInfoResponse =
                client.getConfirmInfo(
                        transferSignResponse.getConfirmTransferLink(exceptionResolver));

        ConfirmTransferResponse confirmTransferResponse =
                client.postConfirmTransfer(
                        confirmInfoResponse.getConfirmationVerificationLink(exceptionResolver));

        if (confirmInfoResponse.needsBankIdSign()) {
            supplementalRequester.openBankId();

            collectBankId(confirmTransferResponse);
        } else {
            confirmTransferResponse.validateResult(exceptionResolver);
        }

        TransferApprovalResponse transferApprovalResponse =
                client.postApproveTransfer(
                        transferSignResponse.getApprovalLink(exceptionResolver),
                        transferApprovalRequest);

        transferApprovalResponse.validateResult(exceptionResolver);
    }

    private void collectBankId(ConfirmTransferResponse confirmTransferResponse) {
        URL confirmExecuteUrl = confirmTransferResponse.getConfirmExecuteLink(exceptionResolver);

        ConfirmTransferResponse pollBankIdResponse;
        BankIdStatus status;

        for (int i = 0; i < Transfers.BANKID_MAX_ATTEMPTS; i++) {
            pollBankIdResponse = client.postConfirmTransfer(confirmExecuteUrl);
            status = pollBankIdResponse.getBankIdStatus();

            switch (status) {
                case DONE:
                    return;
                case WAITING:
                    break;
                case CANCELLED:
                    throw paymentCanceledException(EndUserMessage.BANKID_CANCELLED);
                case FAILED_UNKNOWN:
                    throw paymentFailedException(EndUserMessage.BANKID_TRANSFER_FAILED);
            }

            Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);
        }

        throw paymentCanceledException(EndUserMessage.BANKID_NO_RESPONSE);
    }

    // Made public since it's used to update e-invoices during approval
    public boolean updateIfChanged(Transfer transfer, UpdatablePayment updatablePayment) {
        boolean isUpdated = false;

        if (transfer.getSource() instanceof SwedishIdentifier) {
            transfer.setSource(
                    new SwedishSHBInternalIdentifier(
                            ((SwedishIdentifier) transfer.getSource()).getAccountNumber()));
        }

        Transfer originalTransfer =
                transfer.getOriginalTransfer()
                        .orElseThrow(() -> paymentFailedException(PAYMENT_UPDATE_FAILED));

        if (!Objects.equals(originalTransfer.getHash(), transfer.getHash())) {
            isUpdated = true;

            verifyUpdatePermitted(transfer, originalTransfer, updatablePayment);

            HandelsbankenSEPaymentContext context =
                    (updatablePayment.getContext() != null
                            ? updatablePayment.getContext()
                            : client.paymentContext(updatablePayment));
            verifySourceAccount(transfer.getSource(), context);

            TransferSignResponse response =
                    client.updatePayment(updatablePayment, UpdatePaymentRequest.create(transfer))
                            .orElseThrow(() -> paymentFailedException(PAYMENT_UPDATE_FAILED));

            confirmTransfer(response, null);
        }

        return isUpdated;
    }

    private void verifyUpdatePermitted(
            Transfer transfer, Transfer originalTransfer, UpdatablePayment updatablePayment) {

        if (Objects.equals(originalTransfer.getHash(), transfer.getHash())) {
            return;
        }

        if (!updatablePayment.isChangeAllowed()) {
            throw paymentCanceledException(PAYMENT_UPDATE_NOT_ALLOWED);
        }

        DetailedPermissions permissions = updatablePayment.getDetailedPermissions();

        if (!permissions.isChangeAmount()
                && !transfer.getAmount().equals(originalTransfer.getAmount())) {
            throw paymentCanceledException(PAYMENT_UPDATE_AMOUNT);
        }

        String newDueDate = ThreadSafeDateFormat.FORMATTER_DAILY.format(transfer.getDueDate());
        String originalDueDate =
                ThreadSafeDateFormat.FORMATTER_DAILY.format(originalTransfer.getDueDate());
        if (!permissions.isChangeDate() && !newDueDate.equals(originalDueDate)) {
            throw paymentCanceledException(PAYMENT_UPDATE_DUEDATE);
        }

        if (!permissions.isChangeMessage()
                && !Objects.equals(
                        transfer.getDestinationMessage(),
                        originalTransfer.getDestinationMessage())) {
            throw paymentCanceledException(PAYMENT_UPDATE_DESTINATION_MESSAGE);
        }

        if (!permissions.isChangeFromAccount()
                && !Objects.equals(transfer.getSource(), originalTransfer.getSource())) {
            throw paymentCanceledException(PAYMENT_UPDATE_SOURCE);
        }

        // Source message never editable in SHB API, therefore we always fail if user tries to edit.
        if (!Objects.equals(transfer.getSourceMessage(), originalTransfer.getSourceMessage())) {
            throw paymentCanceledException(PAYMENT_UPDATE_SOURCE_MESSAGE);
        }

        // Destination never editable in SHB API, therefore we always fail if user tries to edit.
        if (!Objects.equals(
                transfer.getDestination().getIdentifier(),
                originalTransfer.getDestination().getIdentifier())) {
            throw paymentCanceledException(PAYMENT_UPDATE_DESTINATION);
        }
    }

    // A lot of exceptions are thrown in this executor, this method saves us a lot of lines
    private TransferExecutionException paymentFailedException(EndUserMessage endUserMessage) {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setEndUserMessage(catalog.getString(endUserMessage))
                .setMessage(endUserMessage.toString())
                .build();
    }

    private TransferExecutionException paymentCanceledException(EndUserMessage endUserMessage) {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(catalog.getString(endUserMessage))
                .setEndUserMessage(catalog.getString(endUserMessage))
                .build();
    }
}
