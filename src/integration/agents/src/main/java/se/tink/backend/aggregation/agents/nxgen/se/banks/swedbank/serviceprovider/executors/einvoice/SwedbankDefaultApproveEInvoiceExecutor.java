package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.einvoice;

import com.google.common.base.Strings;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.BaseTransferExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.SwedbankTransferHelper;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc.RegisterTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc.RegisteredTransfersResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.utilities.SwedbankDateUtils;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.einvoice.rpc.EInvoiceDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.einvoice.rpc.EInvoiceEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.einvoice.rpc.EInvoicePaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinksEntity;
import se.tink.backend.aggregation.nxgen.controllers.transfer.ApproveEInvoiceExecutor;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.enums.TransferPayloadType;
import se.tink.libraries.transfer.rpc.Transfer;

public class SwedbankDefaultApproveEInvoiceExecutor extends BaseTransferExecutor
        implements ApproveEInvoiceExecutor {

    public SwedbankDefaultApproveEInvoiceExecutor(
            SwedbankDefaultApiClient apiClient,
            SwedbankTransferHelper transferHelper,
            SwedbankDateUtils dateUtils) {
        super(apiClient, transferHelper, dateUtils);
    }

    @Override
    public void approveEInvoice(Transfer transfer) throws TransferExecutionException {
        // We'll go through all the profiles to find the one the source account belongs to.
        // That profile will also be selected so it's used going forward in the execution flow.
        String sourceAccountId = this.getSourceAccountIdAndSelectProfile(transfer);

        RegisteredTransfersResponse registeredTransfers = apiClient.registeredTransfers();
        registeredTransfers.noUnsignedTransfersOrThrow();

        RegisteredTransfersResponse registeredTransfersResponse =
                registerEInvoice(transfer, sourceAccountId);

        signAndConfirmTransfer(registeredTransfersResponse);
    }

    private RegisteredTransfersResponse registerEInvoice(
            Transfer transfer, String sourceAccountId) {

        Optional<String> providerUniqueId =
                transfer.getPayloadValue(TransferPayloadType.PROVIDER_UNIQUE_ID);
        if (!providerUniqueId.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(
                            TransferExecutionException.EndUserMessage.EINVOICE_VALIDATE_FAILED)
                    .setMessage(SwedbankBaseConstants.ErrorMessage.EINVOICE_NO_UNIQUE_ID)
                    .build();
        }

        List<EInvoiceEntity> eInvoiceEntities = apiClient.incomingEInvoices();
        Optional<EInvoicePaymentEntity> eInvoicePaymentEntity =
                eInvoiceEntities.stream()
                        .filter(
                                eInvoiceEntity ->
                                        Objects.equals(
                                                providerUniqueId.get(),
                                                eInvoiceEntity.getHashedEinvoiceRefNo()))
                        .map(EInvoiceEntity::getLinks)
                        .map(LinksEntity::getNext)
                        .map(apiClient::eInvoiceDetails)
                        .map(EInvoiceDetailsResponse::getPayment)
                        .filter(
                                eInvoicePayment ->
                                        !Strings.isNullOrEmpty(
                                                eInvoicePayment.getEinvoiceReference()))
                        .filter(eInvoicePayment -> eInvoicePayment.getPayee() != null)
                        .filter(
                                eInvoicePayment ->
                                        !Strings.isNullOrEmpty(eInvoicePayment.getPayee().getId()))
                        .findFirst();

        if (!eInvoicePaymentEntity.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(
                            TransferExecutionException.EndUserMessage.EINVOICE_NO_MATCHES)
                    .setMessage(SwedbankBaseConstants.ErrorMessage.EINVOICE_NO_MATCH)
                    .build();
        }

        RegisterTransferResponse registerTransferResponse =
                getRegisterEinvoice(transfer, sourceAccountId, eInvoicePaymentEntity.get());

        RegisteredTransfersResponse registeredTransfers =
                apiClient.registeredTransfers(registerTransferResponse.getLinks().getNextOrThrow());

        registeredTransfers.oneUnsignedTransferOrThrow();

        Optional<String> idToConfirm = registeredTransfers.getIdToConfirm();
        if (!idToConfirm.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(
                            TransferExecutionException.EndUserMessage.TRANSFER_EXECUTE_FAILED)
                    .setMessage(SwedbankBaseConstants.ErrorMessage.TRANSFER_REGISTER_FAILED)
                    .build();
        }

        return registeredTransfers;
    }

    private RegisterTransferResponse getRegisterEinvoice(
            Transfer transfer,
            String sourceAccountId,
            EInvoicePaymentEntity eInvoicePaymentEntity) {
        Date dueDate = dateUtils.getTransferDateForPayments(transfer.getDueDate());
        try {
            return apiClient.registerEInvoice(
                    transfer.getAmount().getValue(),
                    transfer.getDestinationMessage(),
                    SwedbankTransferHelper.getReferenceTypeFor(transfer),
                    dueDate,
                    eInvoicePaymentEntity.getEinvoiceReference(),
                    eInvoicePaymentEntity.getPayee().getId(),
                    sourceAccountId);
        } catch (HttpResponseException hre) {
            throw convertExceptionIfBadPayment(hre);
        }
    }
}
