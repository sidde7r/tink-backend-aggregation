package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.transfer;

import java.util.Optional;
import java.util.function.Function;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.BaseTransferExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.SwedbankTransferHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc.RegisterTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc.RegisteredTransfersResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.transfer.rpc.RegisterTransferRecipientRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.transfer.rpc.RegisterTransferRecipientResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transferdestination.rpc.PaymentBaseinfoResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.AbstractAccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.transfer.BankTransferExecutor;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.rpc.Transfer;

public class SwedbankDefaultBankTransferExecutor extends BaseTransferExecutor implements BankTransferExecutor {
    private final Catalog catalog;

    public SwedbankDefaultBankTransferExecutor(Catalog catalog, SwedbankDefaultApiClient apiClient,
            SwedbankTransferHelper transferHelper) {
        super(apiClient, transferHelper);
        this.catalog = catalog;
    }

    @Override
    public Optional<String> executeTransfer(Transfer transfer) throws TransferExecutionException {
        // We'll go through all the profiles to find the one the source account belongs to.
        // That profile will be selected so it's used going forward in the execution flow.
        String sourceAccountId = this.getSourceAccountIdAndSelectProfile(transfer);

        RegisteredTransfersResponse registeredTransfers = apiClient.registeredTransfers();
        registeredTransfers.noUnsignedTransfersOrThrow();
        
        RegisteredTransfersResponse registeredTransfersResponse = registerTransfer(transfer, sourceAccountId);

        signAndConfirmTransfer(registeredTransfersResponse);
        return Optional.empty();
    }
    
    private RegisteredTransfersResponse registerTransfer(Transfer transfer, String sourceAccountId) {
        PaymentBaseinfoResponse paymentBaseinfo = apiClient.paymentBaseinfo();

        AccountIdentifier destinationAccount = SwedbankTransferHelper.getDestinationAccount(transfer);
        Optional<String> destinationAccountId = paymentBaseinfo.getTransferDestinationAccountId(destinationAccount);

        // If a registered recipient wasn't found for the destination account, try to register it.
        String recipientAccountId;
        if (destinationAccountId.isPresent()) {
            recipientAccountId = destinationAccountId.get();
        } else {
            AbstractAccountEntity newDestinationAccount = createSignedRecipient(transfer);
            recipientAccountId = newDestinationAccount.getId();
        }

        RegisterTransferResponse registerTransfer = apiClient.registerTransfer(
                transfer.getAmount().getValue(),
                recipientAccountId,
                sourceAccountId);

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

    private AbstractAccountEntity createSignedRecipient(final Transfer transfer) {

        AccountIdentifier accountIdentifier = transfer.getDestination();
        if (accountIdentifier.getType() != AccountIdentifier.Type.SE) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED).setEndUserMessage(
                    catalog.getString("You can only make transfers to Swedish accounts")).build();
        }

        SwedishIdentifier destination = accountIdentifier.to(SwedishIdentifier.class);

        String recipientName = transferHelper.getDestinationName(transfer);

        RegisterTransferRecipientRequest registerTransferRecipientRequest = RegisterTransferRecipientRequest.create(
                destination, recipientName);

        RegisterTransferRecipientResponse registerTransferRecipientResponse = apiClient.registerTransferRecipient(
                registerTransferRecipientRequest);

        return transferHelper.signAndConfirmNewRecipient(registerTransferRecipientResponse.getLinks(),
                findNewRecipientFromPaymentResponse(registerTransferRecipientRequest));
    }

    /**
     * Returns a function that streams through all registered recipients with a filter to find the newly added recipient
     * among them.
     */
    private Function<PaymentBaseinfoResponse, Optional<AbstractAccountEntity>> findNewRecipientFromPaymentResponse(
            RegisterTransferRecipientRequest newRecipientEntity) {

        return confirmResponse -> confirmResponse.getAllRecipientAccounts().stream()
                .filter(account ->
                        account.generalGetAccountIdentifier()
                                .getIdentifier()
                                .replaceAll("[^0-9]", "")
                                .equalsIgnoreCase(newRecipientEntity.getRecipientNumber()))
                .findFirst()
                .map(AbstractAccountEntity.class::cast);
    }
}
