package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.einvoice;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.SwedbankTransferHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc.ConfirmTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc.InitiateSignTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc.RegisterTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc.RegisteredTransfersResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.einvoice.rpc.EInvoiceDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.einvoice.rpc.EInvoiceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.einvoice.rpc.EInvoicePaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transferdestination.rpc.PaymentBaseinfoResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinksEntity;
import se.tink.backend.aggregation.nxgen.controllers.transfer.ApproveEInvoiceExecutor;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.core.transfer.TransferPayloadType;
import se.tink.libraries.account.AccountIdentifier;

public class SwedbankDefaultApproveEInvoiceExecutor implements ApproveEInvoiceExecutor {
    private static final String EMPTY_STRING = "";

    private final SwedbankDefaultApiClient apiClient;
    private final SwedbankTransferHelper transferHelper;

    public SwedbankDefaultApproveEInvoiceExecutor(SwedbankDefaultApiClient apiClient,
            SwedbankTransferHelper transferHelper) {
        this.apiClient = apiClient;
        this.transferHelper = transferHelper;
    }

    @Override
    public void approveEInvoice(Transfer transfer) throws TransferExecutionException {
        // this is the current implementation for transfers, only use the last profile for transfers
        apiClient.selectTransferProfile();

        RegisteredTransfersResponse registeredTransfers = apiClient.registeredTransfers();
        registeredTransfers.noUnsignedTransfersOrThrow();

        RegisteredTransfersResponse registeredTransfersResponse = registerEInvoice(transfer);
        LinksEntity links = registeredTransfersResponse.getLinks();

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

    private RegisteredTransfersResponse registerEInvoice(Transfer transfer) {
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

        Optional<String> providerUniqueId = transfer.getPayloadValue(TransferPayloadType.PROVIDER_UNIQUE_ID);
        if (!providerUniqueId.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(TransferExecutionException.EndUserMessage.EINVOICE_VALIDATE_FAILED)
                    .setMessage(SwedbankBaseConstants.ErrorMessage.EINVOICE_NO_UNIQUE_ID).build();
        }

        List<EInvoiceEntity> eInvoiceEntities = apiClient.incomingEInvoices();
        Optional<String> eInvoiceReference = eInvoiceEntities.stream()
                .filter(eInvoiceEntity ->
                        Objects.equals(providerUniqueId.get(), eInvoiceEntity.getHashedEinvoiceRefNo()))
                .map(EInvoiceEntity::getLinks)
                .map(LinksEntity::getNext)
                .map(apiClient::eInvoiceDetails)
                .map(EInvoiceDetailsResponse::getPayment)
                .map(EInvoicePaymentEntity::getEinvoiceReference)
                .findFirst();

        if (!eInvoiceReference.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(TransferExecutionException.EndUserMessage.EINVOICE_NO_MATCHES)
                    .setMessage(SwedbankBaseConstants.ErrorMessage.EINVOICE_NO_MATCH).build();
        }

        RegisterTransferResponse registerTransferResponse = apiClient.registerEInvoice(
                transfer.getAmount().getValue(),
                transfer.getDestinationMessage(),
                SwedbankTransferHelper.getReferenceTypeFor(transfer),
                transfer.getDueDate(),
                eInvoiceReference.get(),
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
