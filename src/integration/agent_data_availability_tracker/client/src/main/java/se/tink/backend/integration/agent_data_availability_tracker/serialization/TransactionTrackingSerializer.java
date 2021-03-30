package se.tink.backend.integration.agent_data_availability_tracker.serialization;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransactionDate;
import se.tink.backend.aggregation.agents.models.TransactionDateType;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.integration.agent_data_availability_tracker.common.serialization.TrackingList;
import se.tink.backend.integration.agent_data_availability_tracker.common.serialization.TrackingMapSerializer;
import se.tink.libraries.chrono.AvailableDateInformation;

public class TransactionTrackingSerializer extends TrackingMapSerializer {

    private static final String TRANSACTION_ENTITY_NAME = "Transaction";
    private final Transaction transaction;

    public TransactionTrackingSerializer(Transaction transaction, AccountTypes accountType) {
        super(String.format(TRANSACTION_ENTITY_NAME + "<%s>", accountType.toString()));
        this.transaction = transaction;
    }

    private void addFieldForTrackingListBuilder(
            TrackingList.Builder listBuilder,
            Transaction transaction,
            String key,
            Function<Transaction, String> valueExtractor,
            Predicate<Transaction> condition) {
        if (condition.test(transaction)) {
            listBuilder.putRedacted(key, valueExtractor.apply(transaction));
        } else {
            listBuilder.putNull(key);
        }
    }

    @Override
    protected TrackingList populateTrackingMap(TrackingList.Builder listBuilder) {
        listBuilder
                .putRedacted("amount", transaction.getAmount())
                .putRedacted("description", transaction.getDescription())
                .putRedacted("originalAmount", transaction.getOriginalAmount())
                .putListed("type", transaction.getType())
                .putRedacted(
                        "proprietaryFinancialInstitutionType",
                        transaction.getProprietaryFinancialInstitutionType())
                .putRedacted("merchantName", transaction.getMerchantName())
                .putRedacted("merchantCategoryCode", transaction.getMerchantCategoryCode())
                .putRedacted("transactionReference", transaction.getTransactionReference());

        addFieldForTrackingListBuilder(
                listBuilder,
                transaction,
                "date",
                transactionObject -> transactionObject.getDate().toString(),
                transactionObject -> !Objects.isNull(transactionObject.getDate()));

        addFieldForTrackingListBuilder(
                listBuilder,
                transaction,
                "mutability",
                transactionObject -> transactionObject.getMutability().toString(),
                transactionObject -> !Objects.isNull(transactionObject.getMutability()));

        addFieldForTrackingListBuilder(
                listBuilder,
                transaction,
                "transactionId",
                transactionObject ->
                        transactionObject
                                .getExternalSystemIds()
                                .get(TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID),
                transactionObject ->
                        !Objects.isNull(transactionObject.getExternalSystemIds())
                                && transactionObject
                                        .getExternalSystemIds()
                                        .containsKey(
                                                TransactionExternalSystemIdType
                                                        .PROVIDER_GIVEN_TRANSACTION_ID)
                                && !Objects.isNull(
                                        transactionObject
                                                .getExternalSystemIds()
                                                .get(
                                                        TransactionExternalSystemIdType
                                                                .PROVIDER_GIVEN_TRANSACTION_ID)));

        if (!Objects.isNull(transaction.getTransactionAmount())
                && transaction.getTransactionAmount().getDoubleValue() != 0) {
            listBuilder.putRedacted(
                    "transactionAmount", transaction.getTransactionAmount().getDoubleValue());
        } else {
            listBuilder.putNull("transactionAmount");
        }

        for (TransactionDateType type : TransactionDateType.values()) {
            String key = "transactionDate_" + type.toString();
            Optional<TransactionDate> maybeTransactionDate =
                    getTransactionDateByType(transaction.getTransactionDates(), type);
            boolean isDateFound = false;
            if (maybeTransactionDate.isPresent()) {
                Optional<AvailableDateInformation> maybeLocalDate =
                        Optional.of(maybeTransactionDate)
                                .map(Optional::get)
                                .map(TransactionDate::getValue);
                if (maybeLocalDate.isPresent()) {
                    isDateFound = true;
                    listBuilder.putRedacted(key, maybeLocalDate.get().toString());
                }
            }

            if (!isDateFound) {
                listBuilder.putNull(key);
            }
        }

        return listBuilder.build();
    }

    private Optional<TransactionDate> getTransactionDateByType(
            List<TransactionDate> transactionDates, TransactionDateType type) {
        if (transactionDates == null) {
            return Optional.empty();
        }
        return transactionDates.stream().filter(b -> b.getType() == type).findFirst();
    }
}
