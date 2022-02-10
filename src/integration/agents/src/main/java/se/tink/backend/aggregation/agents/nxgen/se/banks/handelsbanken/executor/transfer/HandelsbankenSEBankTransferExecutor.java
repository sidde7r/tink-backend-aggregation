package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants.Transfers;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.ExecutorExceptionResolver;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.payment.HandelsbankenSEPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.entities.ComponentsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.entities.HandelsbankenSEPaymentAccount;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.HandelsbankenSETransferContext;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.TransferApprovalRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.TransferSignRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.TransferSignRequest.AmountableDestination;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.TransferSignResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.ValidateRecipientRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.ValidateRecipientResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.nxgen.controllers.transfer.BankTransferExecutor;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.signableoperation.enums.InternalStatus;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.rpc.Transfer;

public class HandelsbankenSEBankTransferExecutor implements BankTransferExecutor {
    private static final Logger log =
            LoggerFactory.getLogger(HandelsbankenSEBankTransferExecutor.class);

    private final HandelsbankenSEApiClient client;
    private final HandelsbankenSessionStorage sessionStorage;
    private final ExecutorExceptionResolver exceptionResolver;
    private final TransferMessageFormatter transferMessageFormatter;
    private final Catalog catalog;
    private final HandelsbankenSEPaymentExecutor paymentExecutor;

    public HandelsbankenSEBankTransferExecutor(
            HandelsbankenSEApiClient client,
            HandelsbankenSessionStorage sessionStorage,
            ExecutorExceptionResolver exceptionResolver,
            TransferMessageFormatter transferMessageFormatter,
            Catalog catalog,
            HandelsbankenSEPaymentExecutor paymentExecutor) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.exceptionResolver = exceptionResolver;
        this.transferMessageFormatter = transferMessageFormatter;
        this.catalog = catalog;
        this.paymentExecutor = paymentExecutor;
    }

    @Override
    public Optional<String> executeTransfer(Transfer transfer) throws TransferExecutionException {
        if (transfer.getAmount().getValue() < 1) {
            exceptionResolver.cancelTransfer(
                    HandelsbankenSEConstants.Executor.ExceptionMessages.TRANSFER_AMOUNT_TOO_SMALL,
                    InternalStatus.INVALID_MINIMUM_AMOUNT);
        }

        sessionStorage
                .applicationEntryPoint()
                .ifPresent(
                        applicationEntryPoint -> {
                            HandelsbankenSETransferContext transferContext =
                                    client.transferContext(applicationEntryPoint);

                            HandelsbankenSEPaymentAccount sourceAccount =
                                    getSourceAccount(transfer, transferContext);

                            Optional<AmountableDestination> destinationAccount =
                                    getDestinationAccount(transfer, transferContext);

                            AmountableDestination destination =
                                    destinationAccount.orElse(
                                            addNewDestinationAccount(transfer, transferContext));

                            Optional<URL> transferUrl =
                                    getTransferUrl(destination, transferContext);
                            boolean isInternalTransfer =
                                    isTransferBetweenSameUserAccounts(
                                            transfer, transferContext, destination);
                            TransferSignRequest request =
                                    TransferSignRequest.create(
                                            transfer,
                                            sourceAccount,
                                            destination,
                                            generateTransferMessages(transfer, isInternalTransfer),
                                            isInternalTransfer);

                            signTransfer(transferUrl, request);
                        });

        return Optional.empty();
    }

    private HandelsbankenSEPaymentAccount getSourceAccount(
            Transfer transfer, HandelsbankenSETransferContext transferContext) {
        return transferContext
                .findSourceAccount(transfer)
                .orElseThrow(
                        () ->
                                exceptionResolver.cancelTransfer(
                                        HandelsbankenSEConstants.Executor.ExceptionMessages
                                                .SOURCE_ACCOUNT_NOT_FOUND,
                                        InternalStatus.INVALID_SOURCE_ACCOUNT));
    }

    private Optional<AmountableDestination> getDestinationAccount(
            Transfer transfer, HandelsbankenSETransferContext transferContext) {
        return transferContext
                .findDestinationAccount(transfer)
                .map(HandelsbankenSEPaymentAccount::asAmountableDestination);
    }

    private ValidateRecipientResponse addNewDestinationAccount(
            Transfer transfer, HandelsbankenSETransferContext transferContext) {

        ValidateRecipientResponse validateRecipientResponse =
                client.validateRecipient(
                        transferContext, ValidateRecipientRequest.create(transfer));

        if (validateRecipientResponse.getErrors().isEmpty()) {
            return validateRecipientResponse;
        }

        if (Transfers.INVALID_DESTINATION_ACCOUNT.equalsIgnoreCase(
                validateRecipientResponse.getCode())) {
            throw transferCancelledWithMessage(
                    EndUserMessage.INVALID_DESTINATION, InternalStatus.INVALID_DESTINATION_ACCOUNT);
        }

        log.error(
                "Adding of new recipient failed with error code {}, error detail: {}",
                validateRecipientResponse.getCode(),
                validateRecipientResponse.getDetail());

        throw transferFailedWithMessage(EndUserMessage.NEW_RECIPIENT_FAILED);
    }

    private Optional<URL> getTransferUrl(
            AmountableDestination destination, HandelsbankenSETransferContext transferContext) {
        return destination instanceof ValidateRecipientResponse
                ? ((ValidateRecipientResponse) destination).toCreatable()
                : transferContext.toCreatable();
    }

    private void signTransfer(Optional<URL> url, TransferSignRequest request) {
        TransferSignResponse transferSignResponse =
                url.map(requestUrl -> client.signTransfer(requestUrl, request))
                        .orElseThrow(
                                () ->
                                        transferFailedWithMessage(
                                                EndUserMessage.TRANSFER_EXECUTE_FAILED));

        transferSignResponse.validateResponse(exceptionResolver);

        ComponentsEntity componentsEntity = transferSignResponse.getComponentWithForm();
        TransferApprovalRequest approvalRequest =
                TransferApprovalRequest.create(
                        componentsEntity.getFormValue(), componentsEntity.getFormId());

        paymentExecutor.confirmTransfer(transferSignResponse, approvalRequest);
    }

    private TransferMessageFormatter.Messages generateTransferMessages(
            Transfer transfer, boolean isInternalTransfer) {
        return transferMessageFormatter.getMessagesFromRemittanceInformation(
                transfer, isInternalTransfer);
    }

    private boolean isTransferBetweenSameUserAccounts(
            Transfer transfer,
            HandelsbankenSETransferContext transferContext,
            AmountableDestination destinationAccount) {
        return destinationAccount.isKnownDestination()
                && transferContext.destinationIsOwned(transfer);
    }

    private TransferExecutionException transferFailedWithMessage(
            TransferExecutionException.EndUserMessage endUserMessage) {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setEndUserMessage(endUserMessage)
                .setMessage(endUserMessage.toString())
                .build();
    }

    private TransferExecutionException transferCancelledWithMessage(
            EndUserMessage endUserMessage, InternalStatus internalStatus) {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setEndUserMessage(catalog.getString(endUserMessage))
                .setMessage(endUserMessage.toString())
                .setInternalStatus(internalStatus.toString())
                .build();
    }
}
