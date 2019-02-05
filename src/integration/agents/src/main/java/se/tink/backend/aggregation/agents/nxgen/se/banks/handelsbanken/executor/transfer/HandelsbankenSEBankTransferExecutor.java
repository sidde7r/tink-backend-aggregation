package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer;

import java.util.Optional;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.ExecutorExceptionResolver;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.entities.HandelsbankenSEPaymentAccount;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.HandelsbankenSETransferContext;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.TransferSignatureResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.TransferSpecificationRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.TransferSpecificationRequest.AmountableDestination;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.TransferSpecificationResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.ValidateRecipientRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.nxgen.controllers.transfer.BankTransferExecutor;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.libraries.transfer.rpc.Transfer;

public class HandelsbankenSEBankTransferExecutor implements BankTransferExecutor {

    private final HandelsbankenSEApiClient client;
    private final HandelsbankenSessionStorage sessionStorage;
    private final ExecutorExceptionResolver exceptionResolver;
    private final TransferMessageFormatter transferMessageFormatter;

    public HandelsbankenSEBankTransferExecutor(HandelsbankenSEApiClient client,
            HandelsbankenSessionStorage sessionStorage, ExecutorExceptionResolver exceptionResolver,
            TransferMessageFormatter transferMessageFormatter) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.exceptionResolver = exceptionResolver;
        this.transferMessageFormatter = transferMessageFormatter;
    }

    @Override
    public Optional<String> executeTransfer(Transfer transfer) throws TransferExecutionException {
        exceptionResolver
                .throwIf(transfer.getAmount().getValue() < 1,
                        HandelsbankenSEConstants.Executor.ExceptionMessages.TRANSFER_AMOUNT_TOO_SMALL);

        sessionStorage.applicationEntryPoint().ifPresent(applicationEntryPoint -> {
            HandelsbankenSETransferContext transferContext = client.transferContext(applicationEntryPoint);

            HandelsbankenSEPaymentAccount sourceAccount = getSourceAccount(transfer, transferContext);

            AmountableDestination destinationAccount = getDestinationAccount(transfer, transferContext);

            TransferSpecificationResponse transferSpecification =
                    getTransferSpecification(transfer, transferContext, sourceAccount, destinationAccount);

            TransferSignatureResponse transferSignature = signTransfer(transferSpecification);

            transferSignature.validateState(exceptionResolver);
        });
        return Optional.empty();
    }

    private HandelsbankenSEPaymentAccount getSourceAccount(Transfer transfer,
            HandelsbankenSETransferContext transferContext) {
        return transferContext.findSourceAccount(transfer)
                .orElseThrow(() ->
                        exceptionResolver.asException(
                                HandelsbankenSEConstants.Executor.ExceptionMessages.SOURCE_ACCOUNT_NOT_FOUND));
    }

    private AmountableDestination getDestinationAccount(Transfer transfer,
            HandelsbankenSETransferContext transferContext) {
        return transferContext
                .findDestinationAccount(transfer)
                .map(HandelsbankenSEPaymentAccount::asAmountableDestination)
                .orElseGet(() -> client.validateRecipient(transferContext,
                        ValidateRecipientRequest.create(transfer)
                ));
    }

    private TransferSpecificationResponse getTransferSpecification(Transfer transfer,
            HandelsbankenSETransferContext transferContext, HandelsbankenSEPaymentAccount sourceAccount,
            AmountableDestination destinationAccount) {
        Transferable transferable = chooseTransferable(transferContext, destinationAccount);
        return client
                .createTransfer(transferable.toCreatable(exceptionResolver),
                        TransferSpecificationRequest.create(
                                transfer, sourceAccount, destinationAccount,
                                generateTransferMessages(transfer, transferContext, destinationAccount)
                        ));
    }

    private Transferable chooseTransferable(
            HandelsbankenSETransferContext transferContext,
            AmountableDestination destinationAccount) {
        return destinationAccount instanceof Transferable ?
                (Transferable) destinationAccount : transferContext;
    }

    private TransferSignatureResponse signTransfer(TransferSpecificationResponse transferSpecificationResponse) {
        return client.signTransfer(transferSpecificationResponse.toSignable(exceptionResolver));
    }

    private TransferMessageFormatter.Messages generateTransferMessages(Transfer transfer,
            HandelsbankenSETransferContext transferContext,
            AmountableDestination destinationAccount) {
        return transferMessageFormatter
                .getMessages(transfer, isTransferBetweenSameUserAccounts(transfer, transferContext, destinationAccount));
    }

    private boolean isTransferBetweenSameUserAccounts(Transfer transfer, HandelsbankenSETransferContext transferContext,
            AmountableDestination destinationAccount) {
        return destinationAccount.isKnownDestination() && transferContext.destinationIsOwned(transfer);
    }

    public interface Transferable {
        HandelsbankenSEApiClient.Creatable toCreatable(ExecutorExceptionResolver exceptionResolver);
    }
}
