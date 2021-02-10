package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.payment;

import static se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage.INVALID_DESTINATION_MESSAGE;
import static se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage.INVALID_OCR;
import static se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage.PAYMENT_CREATE_FAILED;
import static se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage.PAYMENT_UPDATE_FAILED;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants.Transfers;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.ExecutorExceptionResolver;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.payment.rpc.PaymentSignRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.ConfirmInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.ConfirmTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.TransferApprovalRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.TransferApprovalResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.TransferSignResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.rpc.HandelsbankenSEPaymentContext;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.rpc.PaymentRecipient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator;
import se.tink.backend.aggregation.nxgen.controllers.transfer.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.formatters.DisplayAccountIdentifierFormatter;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.signableoperation.enums.InternalStatus;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.rpc.Transfer;

public class HandelsbankenSEPaymentExecutor implements PaymentExecutor {

    private final SupplementalInformationController supplementalInformationController;
    private final Catalog catalog;
    private final HandelsbankenSEApiClient client;
    private final HandelsbankenSessionStorage sessionStorage;
    private final ExecutorExceptionResolver exceptionResolver;

    public HandelsbankenSEPaymentExecutor(
            SupplementalInformationController supplementalInformationController,
            Catalog catalog,
            HandelsbankenSEApiClient client,
            HandelsbankenSessionStorage sessionStorage,
            ExecutorExceptionResolver exceptionResolver) {
        this.supplementalInformationController = supplementalInformationController;
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
            validateRemittanceInformation(
                    paymentRecipient, transfer.getRemittanceInformation().getValue());
        }
        signTransfer(context.toCreate(), PaymentSignRequest.create(transfer, paymentRecipient));
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

    private void validateRemittanceInformation(
            PaymentRecipient destination, String remittanceInformation) {
        GiroMessageValidator.ValidationResult validationResult =
                GiroMessageValidator.create(destination.getOcrCheck().getValidationConfiguration())
                        .validate(remittanceInformation);

        switch (validationResult.getAllowedType()) {
            case OCR:
                validationResult
                        .getValidOcr()
                        .orElseThrow(
                                () ->
                                        paymentCanceledException(
                                                INVALID_OCR, InternalStatus.INVALID_OCR));
                break;
            case MESSAGE:
                validationResult
                        .getValidMessage()
                        .orElseThrow(
                                () ->
                                        paymentCanceledException(
                                                INVALID_DESTINATION_MESSAGE,
                                                InternalStatus.INVALID_DESTINATION_MESSAGE));
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
                                                                INVALID_DESTINATION_MESSAGE,
                                                                InternalStatus
                                                                        .INVALID_DESTINATION_MESSAGE)));
        }
    }

    private void signTransfer(Optional<URL> url, PaymentSignRequest paymentSignRequest) {
        TransferSignResponse transferSignResponse =
                url.map(requestUrl -> client.signTransfer(requestUrl, paymentSignRequest))
                        .orElseThrow(() -> paymentFailedException(PAYMENT_CREATE_FAILED));
        transferSignResponse.validateResponse(exceptionResolver);

        confirmTransfer(transferSignResponse, null);
    }

    // Made public since it's used in HandelsbankenSEBankTransferExecutor
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
            supplementalInformationController.openMobileBankIdAsync(null);

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
                    throw paymentCanceledException(
                            EndUserMessage.BANKID_CANCELLED, InternalStatus.BANKID_CANCELLED);
                case FAILED_UNKNOWN:
                    throw paymentFailedException(EndUserMessage.BANKID_TRANSFER_FAILED);
            }

            Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);
        }

        throw paymentCanceledException(
                EndUserMessage.BANKID_NO_RESPONSE, InternalStatus.BANKID_NO_RESPONSE);
    }

    // A lot of exceptions are thrown in this executor, this method saves us a lot of lines
    private TransferExecutionException paymentFailedException(EndUserMessage endUserMessage) {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setEndUserMessage(catalog.getString(endUserMessage))
                .setMessage(endUserMessage.toString())
                .build();
    }

    private TransferExecutionException paymentCanceledException(
            EndUserMessage endUserMessage, InternalStatus internalStatus) {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(catalog.getString(endUserMessage))
                .setEndUserMessage(catalog.getString(endUserMessage))
                .setInternalStatus(internalStatus.toString())
                .build();
    }
}
