package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.transferdestination.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBasePredicates;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.PayeeEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.formatters.AccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;
import se.tink.libraries.signableoperation.enums.InternalStatus;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;

@JsonObject
public class PaymentBaseinfoResponse {
    @JsonIgnore
    private static final AccountIdentifierFormatter DEFAULT_FORMAT =
            new DefaultAccountIdentifierFormatter();

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
        return Optional.ofNullable(transactionAccountGroups).orElseGet(Collections::emptyList)
                .stream()
                .map(TransactionAccountGroupEntity::getAccounts)
                .flatMap(Collection::stream)
                .filter(tdae -> tdae.scopesContainsIgnoreCase(scope))
                .collect(Collectors.toList());
    }

    /**
     * Tries to match the given account identifier with an account in transactionAccountGroups, and
     * then return that matching account entity.
     */
    @JsonIgnore
    public Optional<TransferDestinationAccountEntity> getSourceAccount(
            AccountIdentifier accountIdentifier) {
        Preconditions.checkNotNull(accountIdentifier, "The account identifier cannot be null.");
        Preconditions.checkState(
                accountIdentifier.isValid(), "The account identifier must be valid.");

        return Optional.ofNullable(transactionAccountGroups).orElseGet(Collections::emptyList)
                .stream()
                .map(TransactionAccountGroupEntity::getAccounts)
                .flatMap(Collection::stream)
                .filter(tdae -> accountIdentifier.equals(tdae.generalGetAccountIdentifier()))
                .findFirst();
    }

    /**
     * Validates that it's allowed to make transfer from given account entity, if not an exception
     * is thrown. If the ID of this account is null we also throw an exception, since the ID is
     * required to make a transfer.
     */
    @JsonIgnore
    public String validateAndGetSourceAccountId(
            TransferDestinationAccountEntity transferDestinationAccountEntity) {

        transferDestinationAccountEntity.getScopes().stream()
                .filter(SwedbankBaseConstants.TransferScope.TRANSFER_FROM::equalsIgnoreCase)
                .findAny()
                .orElseThrow(
                        () ->
                                TransferExecutionException.builder(
                                                SignableOperationStatuses.CANCELLED)
                                        .setEndUserMessage(
                                                TransferExecutionException.EndUserMessage
                                                        .INVALID_SOURCE)
                                        .setMessage(
                                                SwedbankBaseConstants.ErrorMessage
                                                        .SOURCE_NOT_TRANSFER_CAPABLE)
                                        .setInternalStatus(
                                                InternalStatus.INVALID_SOURCE_ACCOUNT.toString())
                                        .build());

        String transferDestinationAccountId = transferDestinationAccountEntity.getId();

        if (transferDestinationAccountId == null) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(TransferExecutionException.EndUserMessage.SOURCE_NOT_FOUND)
                    .setMessage(SwedbankBaseConstants.ErrorMessage.SOURCE_NOT_FOUND)
                    .setInternalStatus(InternalStatus.INVALID_SOURCE_ACCOUNT.toString())
                    .build();
        }

        return transferDestinationAccountId;
    }

    @JsonIgnore
    public Optional<String> getTransferDestinationAccountId(AccountIdentifier accountIdentifier) {
        Preconditions.checkNotNull(accountIdentifier, "The account identifier cannot be null.");
        Preconditions.checkState(
                accountIdentifier.isValid(), "The account identifier must be valid.");

        Optional<ExternalRecipientEntity> accountEntity =
                Optional.ofNullable(transfer).map(TransferDestinationsEntity::getExternalRecipients)
                        .orElseGet(Collections::emptyList).stream()
                        .filter(SwedbankBasePredicates.filterExternalRecipients(accountIdentifier))
                        .findFirst();

        Optional<TransferDestinationAccountEntity> internalAccountEntity =
                Optional.ofNullable(transactionAccountGroups).orElseGet(Collections::emptyList)
                        .stream()
                        .map(TransactionAccountGroupEntity::getAccounts)
                        .flatMap(Collection::stream)
                        .filter(
                                SwedbankBasePredicates.filterTransferDestinationAccounts(
                                        accountIdentifier))
                        .findFirst();

        if (!accountEntity.isPresent() && !internalAccountEntity.isPresent()) {
            return Optional.empty();
        }

        if (accountEntity.isPresent()) {
            ExternalRecipientEntity externalRecipientEntity = accountEntity.get();
            return Optional.ofNullable(externalRecipientEntity.getId());
        }

        TransferDestinationAccountEntity transferDestinationAccountEntity =
                internalAccountEntity.get();
        return Optional.ofNullable(transferDestinationAccountEntity.getId());
    }

    @JsonIgnore
    public Optional<String> getPaymentDestinationAccountId(AccountIdentifier accountIdentifier) {
        Preconditions.checkNotNull(accountIdentifier, "The account identifier cannot be null.");
        Preconditions.checkState(
                accountIdentifier.isValid(), "The account identifier must be valid.");

        Optional<PayeeEntity> payeeEntity =
                Optional.ofNullable(payment).map(PaymentDestinationsEntity::getPayees)
                        .orElseGet(Collections::emptyList).stream()
                        .filter(SwedbankBasePredicates.filterPayees(accountIdentifier))
                        .findFirst();

        if (!payeeEntity.isPresent()) {
            return Optional.empty();
        }

        return Optional.ofNullable(payeeEntity.get().getId());
    }

    @JsonIgnore
    public List<? extends GeneralAccountEntity> getAllRecipientAccounts() {
        List<GeneralAccountEntity> recipientAccounts =
                new ArrayList<>(getSourceAccounts(SwedbankBaseConstants.TransferScope.TRANSFER_TO));
        recipientAccounts.addAll(getTransferDestinations());

        return recipientAccounts;
    }
}
