package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.transfer;

import java.util.Date;
import java.util.Optional;
import java.util.function.Function;
import org.apache.commons.lang.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankStorage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.BaseTransferExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.SwedbankTransferHelper;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc.RegisterTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc.RegisteredTransfersResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.transfer.rpc.RegisterTransferRecipientRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.transfer.rpc.RegisterTransferRecipientResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.utilities.SwedbankDateUtils;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.utilities.SwedbankNoteToRecipientUtils;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.transferdestination.rpc.PaymentBaseinfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.AbstractAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.BankProfileHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.BankTransferExecutor;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.utils.accountidentifier.IntraBankChecker;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.signableoperation.enums.InternalStatus;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.rpc.Transfer;

public class SwedbankDefaultBankTransferExecutor extends BaseTransferExecutor
        implements BankTransferExecutor {
    private final Catalog catalog;
    private final SwedbankStorage swedbankStorage;

    public SwedbankDefaultBankTransferExecutor(
            Catalog catalog,
            SwedbankDefaultApiClient apiClient,
            SwedbankTransferHelper transferHelper,
            SwedbankStorage swedbankStorage,
            SwedbankDateUtils dateUtils) {
        super(apiClient, transferHelper, dateUtils);
        this.swedbankStorage = swedbankStorage;
        this.catalog = catalog;
    }

    @Override
    public Optional<String> executeTransfer(Transfer transfer) throws TransferExecutionException {
        // We'll go through all the profiles to find the one the source account belongs to.
        // That profile will be selected so it's used going forward in the execution flow.
        if (!SwedbankNoteToRecipientUtils.isValidSwedbankNoteToRecipient(
                transfer.getRemittanceInformation().getValue())) {
            throw createInvalidMessageException();
        }

        String sourceAccountId = this.getSourceAccountIdAndSelectProfile(transfer);

        RegisteredTransfersResponse registeredTransfers = apiClient.registeredTransfers();
        deleteUnsignedRegisteredTransfers(registeredTransfers.getRegisteredTransactions());

        RegisteredTransfersResponse registeredTransfersResponse =
                registerTransfer(transfer, sourceAccountId);

        signAndConfirmTransfer(registeredTransfersResponse);
        return Optional.empty();
    }

    private TransferExecutionException createInvalidMessageException() {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setEndUserMessage(
                        TransferExecutionException.EndUserMessage.INVALID_MESSAGE.getKey().get())
                .setMessage(SwedbankBaseConstants.ErrorMessage.INVALID_DESTINATION_MESSAGE)
                .setInternalStatus(InternalStatus.INVALID_DESTINATION_MESSAGE.toString())
                .build();
    }

    private RegisteredTransfersResponse registerTransfer(
            Transfer transfer, String sourceAccountId) {
        PaymentBaseinfoResponse paymentBaseInfo =
                swedbankStorage.getBankProfileHandler().getActivePaymentBaseInfo();

        AccountIdentifier destinationAccount =
                SwedbankTransferHelper.getDestinationAccount(transfer);

        Optional<String> destinationAccountId =
                paymentBaseInfo.getTransferDestinationAccountId(destinationAccount);

        // If a registered recipient wasn't found for the destination account, try to register it.
        String recipientAccountId = getRecipientAccountId(transfer, destinationAccountId);

        Date dueDate = getDueDate(transfer);

        try {
            RegisterTransferResponse registerTransfer =
                    apiClient.registerTransfer(
                            transfer.getAmount().getValue(),
                            recipientAccountId,
                            transfer.getRemittanceInformation().getValue(),
                            sourceAccountId,
                            dueDate);

            RegisteredTransfersResponse registeredTransfers =
                    apiClient.registeredTransfers(registerTransfer.getLinks().getNextOrThrow());

            registeredTransfers.oneUnsignedTransferOrThrow();

            Optional<String> idToConfirm = registeredTransfers.getIdToConfirm();
            if (!idToConfirm.isPresent()) {
                throw createTransferFailedException();
            }
            return registeredTransfers;
        } catch (HttpResponseException e) {
            throw convertExceptionIfBadPayment(e);
        }
    }

    private String getRecipientAccountId(Transfer transfer, Optional<String> destinationAccountId) {
        String recipientAccountId;
        if (destinationAccountId.isPresent()) {
            recipientAccountId = destinationAccountId.get();
        } else {
            AbstractAccountEntity newDestinationAccount = createSignedRecipient(transfer);
            recipientAccountId = newDestinationAccount.getId();
        }
        return recipientAccountId;
    }

    private Date getDueDate(Transfer transfer) {
        if (IntraBankChecker.isAccountIdentifierIntraBank(
                transfer.getSource(), transfer.getDestination())) {
            return dateUtils.getTransferDateForInternalTransfer(transfer.getDueDate());
        } else {
            return dateUtils.getTransferDateForExternalTransfer(transfer.getDueDate());
        }
    }

    private TransferExecutionException createTransferFailedException() {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setEndUserMessage(
                        TransferExecutionException.EndUserMessage.TRANSFER_EXECUTE_FAILED)
                .setMessage(SwedbankBaseConstants.ErrorMessage.TRANSFER_REGISTER_FAILED)
                .build();
    }

    private AbstractAccountEntity createSignedRecipient(final Transfer transfer) {

        AccountIdentifier accountIdentifier = transfer.getDestination();
        if (accountIdentifier.getType() != AccountIdentifierType.SE) {
            throw createInvalidDestinationException();
        }

        BankProfileHandler handler = swedbankStorage.getBankProfileHandler();
        handler.throwIfNotAuthorizedForRegisterAction(
                SwedbankBaseConstants.MenuItemKey.REGISTER_EXTERNAL_TRANSFER_RECIPIENT, catalog);

        SwedishIdentifier destination = accountIdentifier.to(SwedishIdentifier.class);

        String recipientName = transferHelper.getDestinationName(transfer);

        RegisterTransferRecipientRequest registerTransferRecipientRequest =
                RegisterTransferRecipientRequest.create(destination, recipientName);

        RegisterTransferRecipientResponse registerTransferRecipientResponse =
                apiClient.registerTransferRecipient(registerTransferRecipientRequest);

        return transferHelper.signAndConfirmNewRecipient(
                registerTransferRecipientResponse.getLinks(),
                findNewRecipientFromPaymentResponse(registerTransferRecipientRequest));
    }

    private TransferExecutionException createInvalidDestinationException() {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setEndUserMessage(
                        catalog.getString("You can only make transfers to Swedish accounts"))
                .setInternalStatus(InternalStatus.INVALID_DESTINATION_ACCOUNT.toString())
                .build();
    }

    /**
     * Returns a function that streams through all registered recipients with a filter to find the
     * newly added recipient among them.
     */
    private Function<PaymentBaseinfoResponse, Optional<AbstractAccountEntity>>
            findNewRecipientFromPaymentResponse(
                    RegisterTransferRecipientRequest newRecipientEntity) {

        return confirmResponse ->
                confirmResponse.getAllRecipientAccounts().stream()
                        .filter(account -> getRecipientNumber(newRecipientEntity, account))
                        .findFirst()
                        .map(AbstractAccountEntity.class::cast);
    }

    private boolean getRecipientNumber(
            RegisterTransferRecipientRequest newRecipientEntity, GeneralAccountEntity account) {
        return account.generalGetAccountIdentifier()
                .getIdentifier()
                .replaceAll("[^0-9]", StringUtils.EMPTY)
                .equalsIgnoreCase(newRecipientEntity.getRecipientNumber());
    }
}
