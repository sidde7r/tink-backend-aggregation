package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.transferdestination.rpc;

import static se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants.ErrorMessage.ACCOUNT_ID_NOT_NULL;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants.ErrorMessage.ACCOUNT_ID_NOT_VALID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.Builder;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBasePredicates;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.AbstractAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.PayeeEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.formatters.AccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;
import se.tink.libraries.signableoperation.enums.InternalStatus;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;

@JsonObject
@Getter
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
        validateAccountIdentifier(accountIdentifier);

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
                .orElseThrow(() -> getCancelledTransferExceptionBuilder().build());

        String transferDestinationAccountId = transferDestinationAccountEntity.getId();

        if (transferDestinationAccountId == null) {
            throw getFailedTransferExceptionBuilder().build();
        }

        return transferDestinationAccountId;
    }

    @JsonIgnore
    private Builder getFailedTransferExceptionBuilder() {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setEndUserMessage(TransferExecutionException.EndUserMessage.SOURCE_NOT_FOUND)
                .setMessage(SwedbankBaseConstants.ErrorMessage.SOURCE_NOT_FOUND)
                .setInternalStatus(InternalStatus.INVALID_SOURCE_ACCOUNT.toString());
    }

    @JsonIgnore
    private Builder getCancelledTransferExceptionBuilder() {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setEndUserMessage(TransferExecutionException.EndUserMessage.INVALID_SOURCE)
                .setMessage(SwedbankBaseConstants.ErrorMessage.SOURCE_NOT_TRANSFER_CAPABLE)
                .setInternalStatus(InternalStatus.INVALID_SOURCE_ACCOUNT.toString());
    }

    @JsonIgnore
    public Optional<String> getTransferDestinationAccountId(AccountIdentifier accountIdentifier) {
        validateAccountIdentifier(accountIdentifier);

        Optional<ExternalRecipientEntity> accountEntity =
                getExternalRecipientAccount(accountIdentifier);

        Optional<TransferDestinationAccountEntity> internalAccountEntity =
                getInternalAccount(accountIdentifier);

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
    private void validateAccountIdentifier(AccountIdentifier accountIdentifier) {
        Preconditions.checkNotNull(accountIdentifier, ACCOUNT_ID_NOT_NULL);
        Preconditions.checkState(accountIdentifier.isValid(), ACCOUNT_ID_NOT_VALID);
    }

    @JsonIgnore
    private Optional<TransferDestinationAccountEntity> getInternalAccount(
            AccountIdentifier accountIdentifier) {
        return Optional.ofNullable(transactionAccountGroups).orElseGet(Collections::emptyList)
                .stream()
                .map(TransactionAccountGroupEntity::getAccounts)
                .flatMap(Collection::stream)
                .filter(SwedbankBasePredicates.filterTransferDestinationAccounts(accountIdentifier))
                .findFirst();
    }

    @JsonIgnore
    private Optional<ExternalRecipientEntity> getExternalRecipientAccount(
            AccountIdentifier accountIdentifier) {
        return Optional.ofNullable(transfer).map(TransferDestinationsEntity::getExternalRecipients)
                .orElseGet(Collections::emptyList).stream()
                .filter(SwedbankBasePredicates.filterExternalRecipients(accountIdentifier))
                .findFirst();
    }

    @JsonIgnore
    public Optional<String> getPaymentDestinationAccountId(AccountIdentifier accountIdentifier) {
        validateAccountIdentifier(accountIdentifier);

        Optional<PayeeEntity> payeeEntity =
                Optional.ofNullable(payment).map(PaymentDestinationsEntity::getPayees)
                        .orElseGet(Collections::emptyList).stream()
                        .filter(SwedbankBasePredicates.filterPayees(accountIdentifier))
                        .findFirst();

        return payeeEntity.map(AbstractAccountEntity::getId);
    }

    @JsonIgnore
    public List<? extends GeneralAccountEntity> getAllRecipientAccounts() {
        List<GeneralAccountEntity> recipientAccounts =
                new ArrayList<>(getSourceAccounts(SwedbankBaseConstants.TransferScope.TRANSFER_TO));
        recipientAccounts.addAll(getTransferDestinations());

        return recipientAccounts;
    }
}
