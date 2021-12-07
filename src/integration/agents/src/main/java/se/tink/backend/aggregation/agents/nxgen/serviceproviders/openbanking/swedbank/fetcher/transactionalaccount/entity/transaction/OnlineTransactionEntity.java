package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Strings;
import java.time.LocalDate;
import java.util.Optional;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction.Builder;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateDeserializer;

public class OnlineTransactionEntity extends TransactionEntity {
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate valueDate;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate transactionDate;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate bookingDate;

    @JsonIgnore
    public Transaction toTinkTransaction(boolean isPending, String providerMarket) {
        if (SwedbankConstants.BALTICS.contains(providerMarket)) {
            return toTinkBalticTransaction(isPending, providerMarket);
        } else return toTinkSwedishTransaction(isPending, providerMarket);
    }

    @JsonIgnore
    public Transaction toTinkBalticTransaction(boolean isPending, String providerMarket) {

        Builder builder =
                Transaction.builder()
                        .setAmount(transactionAmount.toTinkAmount())
                        .setDate(Optional.ofNullable(transactionDate).orElse(valueDate))
                        .setDescription(getDescription())
                        .setPending(isPending)
                        .setTransactionDates(getTinkTransactionDates(valueDate, bookingDate))
                        .setProviderMarket(providerMarket)
                        .setProprietaryFinancialInstitutionType(bankTransactionCode)
                        .addExternalSystemIds(
                                TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                                transactionId);

        if (!Strings.isNullOrEmpty(transactionId)) {
            builder.addExternalSystemIds(
                    TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID, transactionId);
        }

        return (Transaction) builder.build();
    }

    @JsonIgnore
    public Transaction toTinkSwedishTransaction(boolean isPending, String providerMarket) {

        Builder builder =
                Transaction.builder()
                        .setAmount(transactionAmount.toTinkAmount())
                        .setDate(Optional.ofNullable(transactionDate).orElse(valueDate))
                        .setDescription(getDescription())
                        .setPending(isPending)
                        .setTransactionDates(getTinkTransactionDates(valueDate, bookingDate))
                        .setProviderMarket(providerMarket);

        if (!Strings.isNullOrEmpty(transactionId)) {
            builder.addExternalSystemIds(
                    TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID, transactionId);
        }

        return (Transaction) builder.build();
    }
}
