package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.transfer;

import java.util.Optional;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.SwedbankTransferHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.transfer.rpc.ConfirmTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.transfer.rpc.InitiateSignTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.transfer.rpc.RegisterTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.transfer.rpc.RegisteredTransfersResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transferdestination.rpc.PaymentBaseinfoResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinksEntity;
import se.tink.backend.aggregation.nxgen.controllers.transfer.BankTransferExecutor;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.account.AccountIdentifier;

public class SwedbankDefaultBankTransferExecutor implements BankTransferExecutor {
    private static final String EMPTY_STRING = "";

    private final SwedbankDefaultApiClient apiClient;
    private final SwedbankTransferHelper transferHelper;

    public SwedbankDefaultBankTransferExecutor(SwedbankDefaultApiClient apiClient,
            SwedbankTransferHelper transferHelper) {
        this.apiClient = apiClient;
        this.transferHelper = transferHelper;
    }

    @Override
    public void executeTransfer(Transfer transfer) throws TransferExecutionException {

        RegisteredTransfersResponse registeredTransfers = apiClient.registeredTransfers();
        registeredTransfers.noUnsignedTransfersOrThrow();

        RegisteredTransfersResponse registeredTransfersResponse = registerTransfer(transfer);
        LinksEntity links = registeredTransfersResponse.getLinks();

        SwedbankTransferHelper.ensureLinksNotNull(links,
                TransferExecutionException.EndUserMessage.TRANSFER_CONFIRM_FAILED,
                SwedbankBaseConstants.ErrorMessage.TRANSFER_CONFIRM_FAILED);

        if (links.getSign() != null) {
            InitiateSignTransferResponse initiateSignTransfer = apiClient.signExternalTransfer(links.getSignOrThrow());
            links = transferHelper.collectBankId(initiateSignTransfer);
        }

        SwedbankTransferHelper.ensureLinksNotNull(links,
                TransferExecutionException.EndUserMessage.TRANSFER_CONFIRM_FAILED,
                SwedbankBaseConstants.ErrorMessage.TRANSFER_CONFIRM_FAILED);

        ConfirmTransferResponse confirmTransferResponse = apiClient.confirmTransfer(links.getNextOrThrow());
        SwedbankTransferHelper.confirmSuccessfulTransfer(confirmTransferResponse,
                registeredTransfersResponse.getIdToConfirm().orElse(EMPTY_STRING));
    }

    private RegisteredTransfersResponse registerTransfer(Transfer transfer) {
        PaymentBaseinfoResponse paymentBaseinfo = apiClient.paymentBaseinfo();

        AccountIdentifier sourceAccount = SwedbankTransferHelper.getSourceAccount(transfer);
        Optional<String> sourceAccountId = paymentBaseinfo.getSourceAccountId(sourceAccount);
        if (!sourceAccountId.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(TransferExecutionException.EndUserMessage.SOURCE_NOT_FOUND)
                    .setMessage(SwedbankBaseConstants.ErrorMessage.SOURCE_NOT_FOUND).build();
        }

        AccountIdentifier destinationAccount = SwedbankTransferHelper.getDestinationAccount(transfer);
        Optional<String> destinationAccountId = paymentBaseinfo.getDestinationAccountId(destinationAccount);
        if (!destinationAccountId.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(TransferExecutionException.EndUserMessage.INVALID_DESTINATION)
                    .setMessage(SwedbankBaseConstants.ErrorMessage.INVALID_DESTINATION).build();
        }

        RegisterTransferResponse registerTransfer = apiClient.registerTransfer(
                transfer.getAmount().getValue(),
                destinationAccountId.get(),
                sourceAccountId.get());

        RegisteredTransfersResponse registeredTransfers = apiClient.registeredTransfers(
                registerTransfer.getLinks().getNextOrThrow());

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
