package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transferdestination.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBasePredicates;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.PayeeEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.formatters.AccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;

@JsonObject
public class PaymentBaseinfoResponse {
    @JsonIgnore
    private static final AccountIdentifierFormatter DEFAULT_FORMAT = new DefaultAccountIdentifierFormatter();
    private PaymentDestinationsEntity payment;
    private TransferDestinationsEntity transfer;
    private AllowedLinksEntity addRecipientStatus;
    private AllowedLinksEntity addPayeeStatus;
    private List<TransactionAccountGroupEntity> transactionAccountGroups;
    private InternationalRecipientsEntity internationalRecipients;

    public PaymentDestinationsEntity getPayment() {
        return payment;
    }

    public TransferDestinationsEntity getTransfer() {
        return transfer;
    }

    public AllowedLinksEntity getAddRecipientStatus() {
        return addRecipientStatus;
    }

    public AllowedLinksEntity getAddPayeeStatus() {
        return addPayeeStatus;
    }

    public List<TransactionAccountGroupEntity> getTransactionAccountGroups() {
        return transactionAccountGroups;
    }

    public InternationalRecipientsEntity getInternationalRecipients() {
        return internationalRecipients;
    }

    @JsonIgnore
    public List<? extends GeneralAccountEntity> getPaymentDestinations() {
        return Optional.ofNullable(payment)
                .map(PaymentDestinationsEntity::getPayees)
                .orElseGet(Collections::emptyList);
    }

    @JsonIgnore
    public List<? extends GeneralAccountEntity> getTransferDestinations() {
        return Optional.ofNullable(transfer)
                .map(TransferDestinationsEntity::getExternalRecipients)
                .orElseGet(Collections::emptyList);
    }

    @JsonIgnore
    public List<? extends GeneralAccountEntity> getPaymentSourceAccounts() {
        return getSourceAccounts(SwedbankBaseConstants.TransferScope.PAYMENT_FROM);
    }

    @JsonIgnore
    public List<? extends GeneralAccountEntity> getTransferSourceAccounts() {
        return getSourceAccounts(SwedbankBaseConstants.TransferScope.TRANSFER_FROM);
    }

    @JsonIgnore
    private List<? extends GeneralAccountEntity> getSourceAccounts(String scope) {
        return Optional.ofNullable(transactionAccountGroups)
                .orElseGet(Collections::emptyList).stream()
                .map(TransactionAccountGroupEntity::getAccounts)
                .flatMap(Collection::stream)
                .filter(tdae -> tdae.scopesContainsIgnoreCase(scope))
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public Optional<String> getSourceAccountId(AccountIdentifier accountIdentifier) {
        Preconditions.checkNotNull(accountIdentifier, "The account identifier cannot be null.");
        Preconditions.checkState(accountIdentifier.isValid(), "The account identifier must be valid.");

        Optional<TransferDestinationAccountEntity> accountEntity = Optional.ofNullable(transactionAccountGroups)
                .orElseGet(Collections::emptyList).stream()
                .map(TransactionAccountGroupEntity::getAccounts)
                .flatMap(Collection::stream)
                .filter(tdae -> accountIdentifier.equals(tdae.generalGetAccountIdentifier()))
                .findFirst();

        if (!accountEntity.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(TransferExecutionException.EndUserMessage.SOURCE_NOT_FOUND)
                    .setMessage(SwedbankBaseConstants.ErrorMessage.SOURCE_NOT_FOUND)
                    .build();
        }

        TransferDestinationAccountEntity transferDestinationAccountEntity = accountEntity.get();

        transferDestinationAccountEntity.getScopes().stream()
                .filter(SwedbankBaseConstants.TransferScope.TRANSFER_FROM::equalsIgnoreCase)
                .findAny()
                .orElseThrow(() -> TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                        .setEndUserMessage(TransferExecutionException.EndUserMessage.INVALID_SOURCE)
                        .setMessage(SwedbankBaseConstants.ErrorMessage.SOURCE_NOT_TRANSFER_CAPABLE)
                        .build());

        return Optional.ofNullable(transferDestinationAccountEntity.getId());
    }

    @JsonIgnore
    public Optional<String> getTransferDestinationAccountId(AccountIdentifier accountIdentifier) {
        Preconditions.checkNotNull(accountIdentifier, "The account identifier cannot be null.");
        Preconditions.checkState(accountIdentifier.isValid(), "The account identifier must be valid.");

        Optional<ExternalRecipientEntity> accountEntity = Optional.ofNullable(transfer)
                .map(TransferDestinationsEntity::getExternalRecipients)
                .orElseGet(Collections::emptyList).stream()
                .filter(SwedbankBasePredicates.filterExternalRecipients(accountIdentifier))
                .findFirst();

        Optional<TransferDestinationAccountEntity> internalAccountEntity = Optional.ofNullable(transactionAccountGroups)
                .orElseGet(Collections::emptyList).stream()
                .map(TransactionAccountGroupEntity::getAccounts)
                .flatMap(Collection::stream)
                .filter(SwedbankBasePredicates.filterTransferDestinationAccounts(accountIdentifier))
                .findFirst();

        // Either it is an external or internal transfer
        if (!accountEntity.isPresent() && !internalAccountEntity.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(TransferExecutionException.EndUserMessage.INVALID_DESTINATION)
                    .setMessage(SwedbankBaseConstants.ErrorMessage.INVALID_DESTINATION)
                    .build();
        }

        if (accountEntity.isPresent()) {
            ExternalRecipientEntity externalRecipientEntity = accountEntity.get();
            return Optional.ofNullable(externalRecipientEntity.getId());
        }

        TransferDestinationAccountEntity transferDestinationAccountEntity = internalAccountEntity.get();
        return Optional.ofNullable(transferDestinationAccountEntity.getId());
    }

    @JsonIgnore
    public Optional<String> getPaymentDestinationAccountId(AccountIdentifier accountIdentifier) {
        Preconditions.checkNotNull(accountIdentifier, "The account identifier cannot be null.");
        Preconditions.checkState(accountIdentifier.isValid(), "The account identifier must be valid.");

        Optional<PayeeEntity> payeeEntity = Optional.ofNullable(payment)
                .map(PaymentDestinationsEntity::getPayees)
                .orElseGet(Collections::emptyList).stream()
                .filter(SwedbankBasePredicates.filterPayees(accountIdentifier))
                .findFirst();

        // Either it is an external or internal transfer
        if (!payeeEntity.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(TransferExecutionException.EndUserMessage.INVALID_DESTINATION)
                    .setMessage(SwedbankBaseConstants.ErrorMessage.INVALID_DESTINATION)
                    .build();
        }

        return Optional.ofNullable(payeeEntity.get().getId());
    }
}
