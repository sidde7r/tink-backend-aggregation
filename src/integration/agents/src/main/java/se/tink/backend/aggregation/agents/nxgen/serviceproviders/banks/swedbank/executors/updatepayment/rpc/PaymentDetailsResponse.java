package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.updatepayment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transferdestination.rpc.PaymentDestinationsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transferdestination.rpc.TransactionAccountGroupEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transferdestination.rpc.TransferDestinationAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.PayeeEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.formatters.AccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;

@JsonObject
public class PaymentDetailsResponse {
    @JsonIgnore
    private static final AccountIdentifierFormatter DEFAULT_FORMAT =
            new DefaultAccountIdentifierFormatter();

    private ConfirmedTransactionEntity transaction;
    private TransactionOptionEntity editTransactionOption;

    public ConfirmedTransactionEntity getTransaction() {
        return transaction;
    }

    public TransactionOptionEntity getEditTransactionOption() {
        return editTransactionOption;
    }

    @JsonIgnore
    public Optional<String> getSourceAccountId(AccountIdentifier accountIdentifier) {
        Preconditions.checkNotNull(accountIdentifier, "The account identifier cannot be null.");
        Preconditions.checkState(
                accountIdentifier.isValid(), "The account identifier must be valid.");

        Optional<TransferDestinationAccountEntity> accountEntity =
                Optional.ofNullable(editTransactionOption)
                        .map(TransactionOptionEntity::getTransactionAccountGroups)
                        .orElseGet(Collections::emptyList).stream()
                        .map(TransactionAccountGroupEntity::getAccounts)
                        .flatMap(Collection::stream)
                        .filter(
                                tdae ->
                                        accountIdentifier.equals(
                                                tdae.generalGetAccountIdentifier()))
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
                                        .build());

        return Optional.ofNullable(transferDestinationAccountEntity.getId());
    }

    @JsonIgnore
    public Optional<String> getPaymentDestinationAccountId(AccountIdentifier accountIdentifier) {
        Preconditions.checkNotNull(accountIdentifier, "The account identifier cannot be null.");
        Preconditions.checkState(
                accountIdentifier.isValid(), "The account identifier must be valid.");

        Optional<PayeeEntity> payeeEntity =
                Optional.ofNullable(editTransactionOption).map(TransactionOptionEntity::getPayment)
                        .map(PaymentDestinationsEntity::getPayees).orElseGet(Collections::emptyList)
                        .stream()
                        .filter(
                                pe -> {
                                    AccountIdentifier peAccountIdentifier =
                                            pe.generalGetAccountIdentifier();
                                    String originalAccountIdentifier =
                                            accountIdentifier.getIdentifier(DEFAULT_FORMAT);

                                    if (peAccountIdentifier == null
                                            || originalAccountIdentifier == null) {
                                        return false;
                                    }

                                    return originalAccountIdentifier.equals(
                                            peAccountIdentifier.getIdentifier(DEFAULT_FORMAT));
                                })
                        .findFirst();

        // Either it is an external or internal transfer
        if (!payeeEntity.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(
                            TransferExecutionException.EndUserMessage.INVALID_DESTINATION)
                    .setMessage(SwedbankBaseConstants.ErrorMessage.INVALID_DESTINATION)
                    .build();
        }

        return Optional.ofNullable(payeeEntity.get().getId());
    }
}
