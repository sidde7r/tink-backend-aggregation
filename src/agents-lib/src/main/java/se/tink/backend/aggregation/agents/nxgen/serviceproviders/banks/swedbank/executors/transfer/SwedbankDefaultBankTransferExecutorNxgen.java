package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.transfer;

import com.google.api.client.http.HttpStatusCodes;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.SwedbankTransferHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc.ConfirmTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc.InitiateSignTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc.RegisterTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc.RegisteredTransfersResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc.TransferTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.transfer.rpc.RegisterTransferRecipientRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.transfer.rpc.RegisterTransferRecipientResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transferdestination.rpc.PaymentBaseinfoResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transferdestination.rpc.TransactionAccountGroupEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transferdestination.rpc.TransferDestinationsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.AbstractAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinksEntity;
import se.tink.backend.aggregation.nxgen.controllers.transfer.nxgen.BankTransferExecutorNxgen;
import se.tink.backend.aggregation.nxgen.controllers.transfer.nxgen.model.Beneficiary;
import se.tink.backend.aggregation.nxgen.controllers.transfer.nxgen.model.OutboxItem;
import se.tink.backend.aggregation.nxgen.controllers.transfer.nxgen.model.TransferDestination;
import se.tink.backend.aggregation.nxgen.controllers.transfer.nxgen.model.TransferSource;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.core.signableoperation.SignableOperationStatuses;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.i18n.Catalog;

public class SwedbankDefaultBankTransferExecutorNxgen implements BankTransferExecutorNxgen {
    private static final Logger log = LoggerFactory.getLogger(SwedbankDefaultBankTransferExecutorNxgen.class);

    private final Catalog catalog;
    private final SwedbankDefaultApiClient apiClient;
    private final SwedbankTransferHelper transferHelper;

    private PaymentBaseinfoResponse paymentBaseinfoResponse;
    private RegisteredTransfersResponse registeredTransfersResponse;

    public SwedbankDefaultBankTransferExecutorNxgen(Catalog catalog, SwedbankDefaultApiClient apiClient,
            SwedbankTransferHelper transferHelper) {
        this.catalog = catalog;
        this.apiClient = apiClient;
        this.transferHelper = transferHelper;
    }

    @Override
    public void initialize() {
        apiClient.selectTransferProfile();
        this.paymentBaseinfoResponse = apiClient.paymentBaseinfo();
    }

    @Override
    public boolean isOutboxEmpty() {
        return apiClient.registeredTransfers().getRegisteredTransactions().isEmpty();
    }

    @Override
    public void addToOutbox(OutboxItem item) {
        RegisterTransferResponse registerTransfer = apiClient.registerTransfer(
                item.getAmount().getValue(),
                item.getDestination().getValueByKey(SwedbankBaseConstants.StorageKey.ID),
                item.getSource().getValueByKey(SwedbankBaseConstants.StorageKey.ID));

        registeredTransfersResponse = apiClient.registeredTransfers(registerTransfer.getLinks().getNextOrThrow());

        registeredTransfersResponse.oneUnsignedTransferOrThrow();

        Optional<String> idToConfirm = registeredTransfersResponse.getIdToConfirm();
        if (!idToConfirm.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(TransferExecutionException.EndUserMessage.TRANSFER_EXECUTE_FAILED)
                    .setMessage(SwedbankBaseConstants.ErrorMessage.TRANSFER_REGISTER_FAILED).build();
        }
    }

    @Override
    public void cleanOutbox() {
        deleteTransfers(apiClient.registeredTransfers().getRegisteredTransactions());
    }

    @Override
    public void signOutbox() {
        LinksEntity links = registeredTransfersResponse.getLinks();
        ConfirmTransferResponse confirmTransferResponse = null;

        Optional<LinkEntity> confirmTransferLink = Optional.ofNullable(links.getNext());

        // Sign the transfer if needed.
        if (!confirmTransferLink.isPresent()) {
            InitiateSignTransferResponse initiateSignTransfer = apiClient
                    .signExternalTransfer(links.getSignOrThrow());
            links = transferHelper.collectBankId(initiateSignTransfer);

            confirmTransferLink = Optional.ofNullable(links.getNext());

            // Prepare for remove of the transfers if the signing failed.
            if (!confirmTransferLink.isPresent()) {
                registeredTransfersResponse = apiClient.registeredTransfers();

                throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                        .setMessage("No confirm transfer link found. Transfer failed.")
                        .setEndUserMessage(TransferExecutionException.EndUserMessage.TRANSFER_CONFIRM_FAILED)
                        .setMessage(SwedbankBaseConstants.ErrorMessage.TRANSFER_CONFIRM_FAILED)
                        .build();
            }
        }

        // Confirm the transfer.
        SwedbankTransferHelper.ensureLinksNotNull(links,
                TransferExecutionException.EndUserMessage.TRANSFER_CONFIRM_FAILED,
                SwedbankBaseConstants.ErrorMessage.TRANSFER_CONFIRM_FAILED);

        confirmTransferResponse = apiClient.confirmTransfer(links.getNextOrThrow());
        SwedbankTransferHelper.confirmSuccessfulTransfer(confirmTransferResponse,
                registeredTransfersResponse.getIdToConfirm().orElse(""));
    }

    @Override
    public Collection<Beneficiary> getBeneficiaries() {
        TransferDestinationsEntity transfer = this.paymentBaseinfoResponse.getTransfer();

        if (transfer == null || transfer.getExternalRecipients() == null) {
            return Collections.emptyList();
        }

        return transfer.getExternalRecipients().stream()
                .map(b -> Beneficiary.builder()
                        .withName(b.getName())
                        .withAccountIdentifier(b.generalGetAccountIdentifier())
                        .withKeyValue(SwedbankBaseConstants.StorageKey.ID, b.getId())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public Beneficiary addBeneficiary(String name, AccountIdentifier identifier) {
        if (identifier.getType() != AccountIdentifier.Type.SE) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setEndUserMessage(catalog.getString("You can only make transfers to Swedish accounts"))
                    .build();
        }

        SwedishIdentifier destination = identifier.to(SwedishIdentifier.class);

        RegisterTransferRecipientRequest registerTransferRecipientRequest = RegisterTransferRecipientRequest.create(
                destination, name);

        RegisterTransferRecipientResponse registerTransferRecipientResponse = apiClient.registerTransferRecipient(
                registerTransferRecipientRequest);

        AbstractAccountEntity entity = transferHelper
                .signAndConfirmNewRecipient(registerTransferRecipientResponse.getLinks(),
                        findNewRecipientFromPaymentResponse(registerTransferRecipientRequest));

        return Beneficiary.builder()
                .withKeyValue(SwedbankBaseConstants.StorageKey.ID, entity.getId())
                .withName(entity.getName())
                .withAccountIdentifier(new SwedishIdentifier(entity.getFullyFormattedNumber()))
                .build();
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

    @Override
    public Collection<TransferSource> getSourceAccounts() {
        List<TransactionAccountGroupEntity> transactionAccountGroups = this.paymentBaseinfoResponse
                .getTransactionAccountGroups();

        if (transactionAccountGroups == null || transactionAccountGroups.isEmpty()) {
            return Lists.newArrayList();
        }

        return transactionAccountGroups.stream().map(TransactionAccountGroupEntity::getAccounts)
                .flatMap(Collection::stream)
                .map(t -> TransferSource.builder()
                        .withKeyValue(SwedbankBaseConstants.StorageKey.ID, t.getId())
                        .withIdentifier(t.generalGetAccountIdentifier())
                        .isTransferable(t.scopesContainsIgnoreCase(SwedbankBaseConstants.TransferScope.TRANSFER_FROM))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public Collection<TransferDestination> getInternalTransferDestinations() {
        List<TransactionAccountGroupEntity> transactionAccountGroups = this.paymentBaseinfoResponse
                .getTransactionAccountGroups();

        return transactionAccountGroups.stream().map(TransactionAccountGroupEntity::getAccounts)
                .flatMap(Collection::stream)
                .map(t -> TransferDestination.builder()
                        .withKeyValue(SwedbankBaseConstants.StorageKey.ID, t.getId())
                        .withIdentifier(t.generalGetAccountIdentifier())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Delete a set of transfer groups (used when cancelling injected transfers).
     */
    private void deleteTransfers(List<TransferTransactionEntity> transferTransactions) {
        for (TransferTransactionEntity transferTransaction : transferTransactions) {
            for (TransactionEntity transactionEntity : transferTransaction.getTransactions()) {
                deleteTransfer(transactionEntity);
            }
        }
    }

    private void deleteTransfer(TransactionEntity transaction) {
        HttpResponse deleteResponse = apiClient.deleteTransfer(transaction.getLinks().getDelete());

        if (deleteResponse.getStatus() != HttpStatusCodes.STATUS_CODE_OK) {
            ErrorResponse errorResponse = deleteResponse.getBody(ErrorResponse.class);
            String errorMessages = errorResponse.getAllErrors();
            log.warn(String.format("#Swedbank-v5 - Delete transfer - Error messages: %s", errorMessages));
        }
    }
}
