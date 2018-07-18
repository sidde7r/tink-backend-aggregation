package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.payment;

import java.util.Optional;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.BaseTransferExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.SwedbankTransferHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc.RegisterTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc.RegisteredTransfersResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transferdestination.rpc.PaymentBaseinfoResponse;
import se.tink.backend.aggregation.nxgen.controllers.transfer.PaymentExecutor;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.account.AccountIdentifier;

public class SwedbankDefaultPaymentExecutor extends BaseTransferExecutor implements PaymentExecutor {
    public SwedbankDefaultPaymentExecutor(SwedbankDefaultApiClient apiClient, SwedbankTransferHelper transferHelper) {
        super(apiClient, transferHelper);
    }

    @Override
    public void executePayment(Transfer transfer) throws TransferExecutionException {
        // this is the current implementation for transfers, only use the last profile for transfers
        apiClient.selectTransferProfile();

        RegisteredTransfersResponse registeredTransfers = apiClient.registeredTransfers();
        registeredTransfers.noUnsignedTransfersOrThrow();

        RegisteredTransfersResponse registeredTransfersResponse = registerPayment(transfer);

        signAndConfirmTransfer(registeredTransfersResponse);
    }

    private RegisteredTransfersResponse registerPayment(Transfer transfer) {
        PaymentBaseinfoResponse paymentBaseinfo = apiClient.paymentBaseinfo();

        AccountIdentifier sourceAccount = SwedbankTransferHelper.getSourceAccount(transfer);
        Optional<String> sourceAccountId = paymentBaseinfo.getSourceAccountId(sourceAccount);
        if (!sourceAccountId.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(TransferExecutionException.EndUserMessage.SOURCE_NOT_FOUND)
                    .setMessage(SwedbankBaseConstants.ErrorMessage.SOURCE_NOT_FOUND).build();
        }

        AccountIdentifier destinationAccount = SwedbankTransferHelper.getDestinationAccount(transfer);
        Optional<String> destinationAccountId = paymentBaseinfo.getPaymentDestinationAccountId(destinationAccount);
        if (!destinationAccountId.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(TransferExecutionException.EndUserMessage.INVALID_DESTINATION)
                    .setMessage(SwedbankBaseConstants.ErrorMessage.INVALID_DESTINATION).build();
        }

        RegisterTransferResponse registerTransferResponse = apiClient.registerPayment(
                transfer.getAmount().getValue(),
                transfer.getDestinationMessage(),
                SwedbankTransferHelper.getReferenceTypeFor(transfer),
                transfer.getDueDate(),
                destinationAccountId.get(),
                sourceAccountId.get());

        RegisteredTransfersResponse registeredTransfers = apiClient.registeredTransfers(
                registerTransferResponse.getLinks().getNextOrThrow());

        registeredTransfers.oneUnsignedTransferOrThrow();

        Optional<String> idToConfirm = registeredTransfers.getIdToConfirm();
        if (!idToConfirm.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(TransferExecutionException.EndUserMessage.TRANSFER_EXECUTE_FAILED)
                    .setMessage(SwedbankBaseConstants.ErrorMessage.TRANSFER_REGISTER_FAILED).build();
        }

        return registeredTransfers;
    }
}
