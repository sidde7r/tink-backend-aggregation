package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.updatepayment;

import java.util.Optional;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.SwedbankTransferHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc.ConfirmTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc.InitiateSignTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc.RegisterTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc.RegisteredTransfersResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.updatepayment.rpc.ConfirmedTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.updatepayment.rpc.PaymentDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.updatepayment.rpc.PaymentsConfirmedResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinksEntity;
import se.tink.backend.aggregation.nxgen.controllers.transfer.UpdatePaymentExecutor;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.account.AccountIdentifier;

public class SwedbankDefaultUpdatePaymentExecutor implements UpdatePaymentExecutor {
    private static final String EMPTY_STRING = "";

    private final SwedbankDefaultApiClient apiClient;
    private final SwedbankTransferHelper transferHelper;

    public SwedbankDefaultUpdatePaymentExecutor(SwedbankDefaultApiClient apiClient,
            SwedbankTransferHelper transferHelper) {
        this.apiClient = apiClient;
        this.transferHelper = transferHelper;
    }

    @Override
    public void updatePayment(Transfer transfer) throws TransferExecutionException {

        RegisteredTransfersResponse registeredTransfers = apiClient.registeredTransfers();
        registeredTransfers.noUnsignedTransfersOrThrow();

        Optional<Transfer> originalTransfer = transfer.getOriginalTransfer();

        if (!originalTransfer.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(TransferExecutionException.EndUserMessage.PAYMENT_UPDATE_FAILED)
                    .setMessage("The original transfer was not available.")
                    .build();
        }

        PaymentsConfirmedResponse paymentsConfirmedResponse = apiClient.paymentsConfirmed();

        Optional<ConfirmedTransactionEntity> confirmedPayment = paymentsConfirmedResponse
                .getConfirmedPayment(originalTransfer.get());

        if (!confirmedPayment.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(TransferExecutionException.EndUserMessage.PAYMENT_NO_MATCHES)
                    .setMessage("Could not match transfer to any existing transfers at the bank.")
                    .build();
        }

        LinksEntity links = confirmedPayment.get().getLinks();
        if (links == null) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setEndUserMessage(TransferExecutionException.EndUserMessage.PAYMENT_NO_MATCHES)
                    .setMessage("Could not match transfer to any existing transfers at the bank.")
                    .build();
        }

        PaymentDetailsResponse paymentDetailsResponse = apiClient.paymentDetails(links.getSelf());

        AccountIdentifier sourceAccount = SwedbankTransferHelper.getSourceAccount(transfer);
        Optional<String> sourceAccountId = paymentDetailsResponse.getSourceAccountId(sourceAccount);
        if (!sourceAccountId.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(TransferExecutionException.EndUserMessage.SOURCE_NOT_FOUND)
                    .setMessage(SwedbankBaseConstants.ErrorMessage.SOURCE_NOT_FOUND).build();
        }

        AccountIdentifier destinationAccount = SwedbankTransferHelper.getDestinationAccount(transfer);
        Optional<String> destinationAccountId = paymentDetailsResponse.getPaymentDestinationAccountId(destinationAccount);
        if (!destinationAccountId.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(TransferExecutionException.EndUserMessage.INVALID_DESTINATION)
                    .setMessage(SwedbankBaseConstants.ErrorMessage.INVALID_DESTINATION).build();
        }

        Optional<LinkEntity> editLink = Optional.ofNullable(paymentDetailsResponse.getTransaction())
                .map(ConfirmedTransactionEntity::getLinks)
                .map(LinksEntity::getEdit);

        if (!editLink.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(TransferExecutionException.EndUserMessage.INVALID_DESTINATION)
                    .setMessage(SwedbankBaseConstants.ErrorMessage.INVALID_DESTINATION).build();
        }

        RegisterTransferResponse registerTransferResponse = apiClient.updatePayment(editLink.get(),
                transfer.getAmount().getValue(),
                transfer.getDestinationMessage(),
                SwedbankTransferHelper.getReferenceTypeFor(transfer),
                transfer.getDueDate(),
                destinationAccountId.get(),
                sourceAccountId.get());

        RegisteredTransfersResponse registeredTransfersResponse = apiClient.registeredTransfers(
                registerTransferResponse.getLinks().getNextOrThrow());

        registeredTransfersResponse.oneUnsignedTransferOrThrow();

        Optional<String> idToConfirm = registeredTransfersResponse.getIdToConfirm();
        if (!idToConfirm.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(TransferExecutionException.EndUserMessage.TRANSFER_EXECUTE_FAILED)
                    .setMessage(SwedbankBaseConstants.ErrorMessage.TRANSFER_REGISTER_FAILED).build();
        }

        links = registeredTransfersResponse.getLinks();

        SwedbankTransferHelper.ensureLinksNotNull(links,
                TransferExecutionException.EndUserMessage.TRANSFER_CONFIRM_FAILED,
                SwedbankBaseConstants.ErrorMessage.TRANSFER_CONFIRM_FAILED);

        InitiateSignTransferResponse initiateSignTransfer = apiClient.signExternalTransfer(links.getSignOrThrow());
        links = transferHelper.collectBankId(initiateSignTransfer);

        SwedbankTransferHelper.ensureLinksNotNull(links,
                TransferExecutionException.EndUserMessage.TRANSFER_CONFIRM_FAILED,
                SwedbankBaseConstants.ErrorMessage.TRANSFER_CONFIRM_FAILED);

        ConfirmTransferResponse confirmTransferResponse = apiClient.confirmTransfer(links.getNextOrThrow());
        SwedbankTransferHelper.confirmSuccessfulTransfer(confirmTransferResponse,
                registeredTransfersResponse.getIdToConfirm().orElse(EMPTY_STRING));
    }
}
