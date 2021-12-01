package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.fetcher.transactions;

import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.TransactionMutability;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.entities.transaction.SupplementaryData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.entities.transaction.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.rpc.transaction.TransactionMapper;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction.Builder;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class MonzoTransactionMapper implements TransactionMapper {

    @Override
    public Transaction toTinkTransaction(TransactionEntity transactionEntity) {
        Builder builder =
                Transaction.builder()
                        .setAmount(transactionEntity.getSignedAmount())
                        .setPending(transactionEntity.isPending())
                        .setDate(transactionEntity.getDateOfTransaction())
                        .setTransactionDates(transactionEntity.getTransactionDates())
                        .setTransactionReference(transactionEntity.getTransactionReference())
                        .setProviderMarket(getProviderMarket())
                        .setDescription(getTransactionDescription(transactionEntity));

        // Do not specify mutability at all if there is no TransactionMutability field provided
        // Necessary for backwards compatibility (before v3.1.5) when Booked transactions could be
        // mutable
        TransactionMutability mutability = transactionEntity.getMutability();
        if (mutability == TransactionMutability.MUTABLE) {
            builder.setMutable(true);
        } else if (mutability == TransactionMutability.IMMUTABLE) {
            builder.setMutable(false);
        }

        transactionEntity
                .getTransactionId()
                .ifPresent(
                        id ->
                                builder.addExternalSystemIds(
                                        TransactionExternalSystemIdType
                                                .PROVIDER_GIVEN_TRANSACTION_ID,
                                        id));
        transactionEntity
                .getMerchantDetails()
                .ifPresent(
                        details ->
                                builder.setMerchantName(details.getMerchantName())
                                        .setMerchantCategoryCode(
                                                details.getMerchantCategoryCode()));
        transactionEntity
                .getProprietaryBankTransactionCode()
                .ifPresent(builder::setProprietaryFinancialInstitutionType);
        return (Transaction) builder.build();
    }

    @Override
    public CreditCardTransaction toTinkCreditCardTransaction(
            TransactionEntity transactionEntity, CreditCardAccount account) {
        Builder builder =
                CreditCardTransaction.builder()
                        .setCreditCardAccountNumber(
                                account != null ? account.getAccountNumber() : null)
                        .setAmount(transactionEntity.getSignedAmount())
                        .setPending(transactionEntity.isPending())
                        .setDate(transactionEntity.getDateOfTransaction())
                        .setTransactionDates(transactionEntity.getTransactionDates())
                        .setTransactionReference(transactionEntity.getTransactionReference())
                        .setProviderMarket(getProviderMarket())
                        .setDescription(getTransactionDescription(transactionEntity));

        // Set mutability flag only if there is TransactionMutability field provided
        // Necessary for backwards compatibility (before v3.1.5) when Booked transactions could be
        // mutable
        TransactionMutability mutability = transactionEntity.getMutability();
        if (mutability == TransactionMutability.MUTABLE) {
            builder.setMutable(true);
        } else if (mutability == TransactionMutability.IMMUTABLE) {
            builder.setMutable(false);
        }

        transactionEntity
                .getTransactionId()
                .ifPresent(
                        id ->
                                builder.addExternalSystemIds(
                                        TransactionExternalSystemIdType
                                                .PROVIDER_GIVEN_TRANSACTION_ID,
                                        id));
        transactionEntity
                .getMerchantDetails()
                .ifPresent(
                        details ->
                                builder.setMerchantName(details.getMerchantName())
                                        .setMerchantCategoryCode(
                                                details.getMerchantCategoryCode()));
        transactionEntity
                .getProprietaryBankTransactionCode()
                .ifPresent(builder::setProprietaryFinancialInstitutionType);
        return (CreditCardTransaction) builder.build();
    }

    private static String getTransactionDescription(TransactionEntity transactionEntity) {
        return Optional.ofNullable(transactionEntity.getSupplementaryData())
                .filter(
                        supplementaryData ->
                                Objects.nonNull(supplementaryData.getRawTransactionDescription()))
                .map(SupplementaryData::getRawTransactionDescription)
                .orElseGet(transactionEntity::getDescription);
    }
}
