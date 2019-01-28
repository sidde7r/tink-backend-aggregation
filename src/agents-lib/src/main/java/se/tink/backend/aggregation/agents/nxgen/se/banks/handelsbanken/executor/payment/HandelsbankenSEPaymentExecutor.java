package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.payment;

import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.entities.DetailedPermissions;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.rpc.UpdatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount.rpc.PaymentDetails;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.interfaces.UpdatablePayment;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.rpc.HandelsbankenSEPaymentContext;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.rpc.PaymentRecipient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator;
import se.tink.backend.aggregation.nxgen.controllers.transfer.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.transfer.UpdatePaymentExecutor;
import se.tink.backend.core.signableoperation.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.SwedishSHBInternalIdentifier;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.formatters.DisplayAccountIdentifierFormatter;
import se.tink.libraries.date.ThreadSafeDateFormat;
import static se.tink.backend.aggregation.agents.TransferExecutionException.EndUserMessage.INVALID_DESTINATION_MESSAGE;
import static se.tink.backend.aggregation.agents.TransferExecutionException.EndUserMessage.INVALID_OCR;
import static se.tink.backend.aggregation.agents.TransferExecutionException.EndUserMessage.PAYMENT_CREATE_FAILED;
import static se.tink.backend.aggregation.agents.TransferExecutionException.EndUserMessage.PAYMENT_NO_MATCHES;
import static se.tink.backend.aggregation.agents.TransferExecutionException.EndUserMessage.PAYMENT_UPDATE_AMOUNT;
import static se.tink.backend.aggregation.agents.TransferExecutionException.EndUserMessage.PAYMENT_UPDATE_DESTINATION;
import static se.tink.backend.aggregation.agents.TransferExecutionException.EndUserMessage.PAYMENT_UPDATE_DESTINATION_MESSAGE;
import static se.tink.backend.aggregation.agents.TransferExecutionException.EndUserMessage.PAYMENT_UPDATE_DUEDATE;
import static se.tink.backend.aggregation.agents.TransferExecutionException.EndUserMessage.PAYMENT_UPDATE_FAILED;
import static se.tink.backend.aggregation.agents.TransferExecutionException.EndUserMessage.PAYMENT_UPDATE_NOT_ALLOWED;
import static se.tink.backend.aggregation.agents.TransferExecutionException.EndUserMessage.PAYMENT_UPDATE_SOURCE;
import static se.tink.backend.aggregation.agents.TransferExecutionException.EndUserMessage.PAYMENT_UPDATE_SOURCE_MESSAGE;

public class HandelsbankenSEPaymentExecutor implements PaymentExecutor, UpdatePaymentExecutor {

    private final HandelsbankenSEApiClient client;
    private final HandelsbankenSessionStorage sessionStorage;

    public HandelsbankenSEPaymentExecutor(
            HandelsbankenSEApiClient client,
            HandelsbankenSessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void executePayment(Transfer transfer) throws TransferExecutionException {
        HandelsbankenSEPaymentContext context = sessionStorage.applicationEntryPoint()
                .map(client::paymentContext)
                .orElseThrow(() -> exception(PAYMENT_CREATE_FAILED));
        verifySourceAccount(transfer.getSource(), context);

        PaymentRecipient paymentRecipient = verifyRecipient(transfer.getDestination(), context);
        validateDestinationMessage(paymentRecipient, transfer.getDestinationMessage());

        CreatePaymentRequest createPaymentRequest = CreatePaymentRequest.create(transfer, paymentRecipient);
        PaymentDetails paymentDetails = client.createPayment(context, createPaymentRequest)
                .orElseThrow(() -> exception(PAYMENT_CREATE_FAILED));

        client.signPayment(paymentDetails);
    }

    @Override
    public void updatePayment(Transfer transfer) throws TransferExecutionException {
        PaymentDetails paymentDetails = fetchPaymentDetails(transfer.getOriginalTransfer()
                .orElseThrow((() -> exception(PAYMENT_UPDATE_FAILED))));
        paymentDetails = (PaymentDetails) updateIfChanged(transfer, paymentDetails);
        signUpdate(paymentDetails);
    }

    private void verifySourceAccount(AccountIdentifier source, HandelsbankenSEPaymentContext context) {
        String sourceNumber = (source instanceof SwedishIdentifier
                ? ((SwedishIdentifier) source).getAccountNumber()
                : source.getIdentifier());

        context.retrieveOwnedSourceAccounts().stream()
                .filter(generalAccountEntity -> {
                    AccountIdentifier account = generalAccountEntity.generalGetAccountIdentifier();
                    String accountNumber = (account instanceof SwedishIdentifier
                            ? ((SwedishIdentifier) account).getAccountNumber()
                            : account.getIdentifier());
                    return accountNumber.equals(sourceNumber);
                })
                .findFirst()
                .orElseThrow(() -> exception(PAYMENT_UPDATE_FAILED));
    }

    private PaymentRecipient verifyRecipient(AccountIdentifier destination, HandelsbankenSEPaymentContext paymentContext) {
        return paymentContext.paymentRecipients()
                .stream()
                .filter(recipient -> {
                    DefaultAccountIdentifierFormatter defaultFormatter = new DefaultAccountIdentifierFormatter();
                    return Objects.equals(recipient
                                    .accountIdentifier()
                                    .getIdentifier(defaultFormatter),
                            destination.getIdentifier(defaultFormatter));
                })
                .findFirst()
                .orElse(client.lookupRecipient(paymentContext,
                        destination.getIdentifier(new DisplayAccountIdentifierFormatter())));
    }

    private void validateDestinationMessage(PaymentRecipient destination, String destinationMessage) {
        GiroMessageValidator.ValidationResult validationResult = GiroMessageValidator
                .create(destination.getOcrCheck().getValidationConfiguration())
                .validate(destinationMessage);

        switch (validationResult.getAllowedType()) {
        case OCR:
            validationResult.getValidOcr().orElseThrow(() -> exception(INVALID_OCR));
            break;
        case MESSAGE:   // Intentional fallthrough
        default:
            validationResult.getValidOcr().orElseThrow(() -> exception(INVALID_DESTINATION_MESSAGE));
        }
    }

    private PaymentDetails fetchPaymentDetails(Transfer originalTransfer) {
        return sessionStorage.applicationEntryPoint()
                .map(client::pendingTransactions)
                .orElseThrow((() -> exception(PAYMENT_UPDATE_FAILED)))
                .getPendingTransactionStream()
                .map(client::paymentDetails)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(paymentDetails1 ->
                        Objects.equals(originalTransfer.getHash(), paymentDetails1.toTransfer().getHash()))
                .findFirst()
                .orElseThrow(() -> exception(PAYMENT_NO_MATCHES));
    }

    // Made public since it's used to update e-invoices during approval
    public UpdatablePayment updateIfChanged(Transfer transfer, UpdatablePayment updatablePayment) {

        if (transfer.getSource() instanceof  SwedishIdentifier) {
            transfer.setSource(new SwedishSHBInternalIdentifier(
                    ((SwedishIdentifier) transfer.getSource()).getAccountNumber())
            );
        }

        Transfer originalTransfer = transfer.getOriginalTransfer()
                .orElseThrow(() -> exception(PAYMENT_UPDATE_FAILED));

        if (!Objects.equals(originalTransfer.getHash(), transfer.getHash())) {
            verifyUpdatePermitted(transfer, originalTransfer, updatablePayment);

            HandelsbankenSEPaymentContext context = (updatablePayment.getContext() != null
                    ? updatablePayment.getContext()
                    : client.paymentContext(updatablePayment));
            verifySourceAccount(transfer.getSource(), context);

            updatablePayment = client
                    .updatePayment(updatablePayment, UpdatePaymentRequest.create(transfer))
                    .orElseThrow(() -> exception(PAYMENT_UPDATE_FAILED));
        }

        return updatablePayment;
    }

    private void signUpdate(PaymentDetails paymentDetails) {
        client.signPayment(paymentDetails);
    }

    private void verifyUpdatePermitted(Transfer transfer, Transfer originalTransfer,
            UpdatablePayment updatablePayment) {

        if (Objects.equals(originalTransfer.getHash(), transfer.getHash())) {
            return;
        }

        if (!updatablePayment.isChangeAllowed()) {
            throw exception(PAYMENT_UPDATE_NOT_ALLOWED);
        }

        DetailedPermissions permissions = updatablePayment.getDetailedPermissions();

        if (!permissions.isChangeAmount() && !transfer.getAmount().equals(originalTransfer.getAmount())) {
            throw exception(PAYMENT_UPDATE_AMOUNT);
        }

        String newDueDate = ThreadSafeDateFormat.FORMATTER_DAILY.format(transfer.getDueDate());
        String originalDueDate = ThreadSafeDateFormat.FORMATTER_DAILY.format(originalTransfer.getDueDate());
        if (!permissions.isChangeDate() && !newDueDate.equals(originalDueDate)) {
            throw exception(PAYMENT_UPDATE_DUEDATE);
        }

        if (!permissions.isChangeMessage() &&
                !Objects.equals(transfer.getDestinationMessage(), originalTransfer.getDestinationMessage())) {
            throw exception(PAYMENT_UPDATE_DESTINATION_MESSAGE);
        }

        if (!permissions.isChangeFromAccount() && !Objects.equals(transfer.getSource(), originalTransfer.getSource())) {
            throw exception(PAYMENT_UPDATE_SOURCE);
        }

        // Source message never editable in SHB API, therefore we always fail if user tries to edit.
        if (!Objects.equals(transfer.getSourceMessage(), originalTransfer.getSourceMessage())) {
            throw exception(PAYMENT_UPDATE_SOURCE_MESSAGE);
        }

        // Destination never editable in SHB API, therefore we always fail if user tries to edit.
        if (!Objects.equals(transfer.getDestination().getIdentifier(),
                originalTransfer.getDestination().getIdentifier())) {
            throw exception(PAYMENT_UPDATE_DESTINATION);
        }
    }

    // A lot of exceptions are thrown in this executor, this method saves us a lot of lines
    private TransferExecutionException exception(TransferExecutionException.EndUserMessage endUserMessage) {
        return TransferExecutionException
                .builder(SignableOperationStatuses.FAILED)
                .setEndUserMessage(endUserMessage)
                .setMessage(endUserMessage.toString())
                .build();
    }
}
