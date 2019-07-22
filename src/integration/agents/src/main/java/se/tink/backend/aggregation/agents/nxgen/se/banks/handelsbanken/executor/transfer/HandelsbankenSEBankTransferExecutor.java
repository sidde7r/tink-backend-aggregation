package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer;

import java.util.Optional;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.TransferExecutionException.EndUserMessage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants;
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
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.rpc.Transfer;

public class HandelsbankenSEBankTransferExecutor implements BankTransferExecutor {

    private final HandelsbankenSEApiClient client;
    private final HandelsbankenSessionStorage sessionStorage;
    private final ExecutorExceptionResolver exceptionResolver;
    private final TransferMessageFormatter transferMessageFormatter;
    private final HandelsbankenSEPaymentExecutor paymentExecutor;

    public HandelsbankenSEBankTransferExecutor(
            HandelsbankenSEApiClient client,
            HandelsbankenSessionStorage sessionStorage,
            ExecutorExceptionResolver exceptionResolver,
            TransferMessageFormatter transferMessageFormatter) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.exceptionResolver = exceptionResolver;
        this.transferMessageFormatter = transferMessageFormatter;
        this.paymentExecutor =
                new HandelsbankenSEPaymentExecutor(client, sessionStorage, exceptionResolver);
    }

    @Override
    public Optional<String> executeTransfer(Transfer transfer) throws TransferExecutionException {
        exceptionResolver.throwIf(
                transfer.getAmount().getValue() < 1,
                HandelsbankenSEConstants.Executor.ExceptionMessages.TRANSFER_AMOUNT_TOO_SMALL);

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

                            TransferSignRequest request =
                                    TransferSignRequest.create(
                                            transfer,
                                            sourceAccount,
                                            destination,
                                            generateTransferMessages(
                                                    transfer, transferContext, destination));

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
                                exceptionResolver.asException(
                                        HandelsbankenSEConstants.Executor.ExceptionMessages
                                                .SOURCE_ACCOUNT_NOT_FOUND));
    }

    private Optional<AmountableDestination> getDestinationAccount(
            Transfer transfer, HandelsbankenSETransferContext transferContext) {
        return transferContext
                .findDestinationAccount(transfer)
                .map(HandelsbankenSEPaymentAccount::asAmountableDestination);
    }

    private ValidateRecipientResponse addNewDestinationAccount(
            Transfer transfer, HandelsbankenSETransferContext transferContext) {
        return client.validateRecipient(transferContext, ValidateRecipientRequest.create(transfer));
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
                        .orElseThrow(() -> exception(EndUserMessage.TRANSFER_EXECUTE_FAILED));

        transferSignResponse.validateResponse(exceptionResolver);

        ComponentsEntity componentsEntity = transferSignResponse.getComponentWithForm();
        TransferApprovalRequest approvalRequest =
                TransferApprovalRequest.create(
                        componentsEntity.getFormValue(), componentsEntity.getFormId());

        paymentExecutor.confirmTransfer(transferSignResponse, approvalRequest);
    }

    private TransferMessageFormatter.Messages generateTransferMessages(
            Transfer transfer,
            HandelsbankenSETransferContext transferContext,
            AmountableDestination destinationAccount) {
        return transferMessageFormatter.getMessages(
                transfer,
                isTransferBetweenSameUserAccounts(transfer, transferContext, destinationAccount));
    }

    private boolean isTransferBetweenSameUserAccounts(
            Transfer transfer,
            HandelsbankenSETransferContext transferContext,
            AmountableDestination destinationAccount) {
        return destinationAccount.isKnownDestination()
                && transferContext.destinationIsOwned(transfer);
    }

    private TransferExecutionException exception(
            TransferExecutionException.EndUserMessage endUserMessage) {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setEndUserMessage(endUserMessage)
                .setMessage(endUserMessage.toString())
                .build();
    }
}
