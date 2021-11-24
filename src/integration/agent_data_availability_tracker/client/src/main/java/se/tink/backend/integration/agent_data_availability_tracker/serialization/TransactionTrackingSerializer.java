package se.tink.backend.integration.agent_data_availability_tracker.serialization;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.from(ZoneOffset.UTC));
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
            Predicate<Transaction> condition,
            boolean shouldRedact) {
        if (condition.test(transaction)) {
            if (shouldRedact) {
                listBuilder.putRedacted(key, valueExtractor.apply(transaction));
            } else {
                listBuilder.putListed(key, valueExtractor.apply(transaction));
            }
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
                transactionObject -> DATE_FORMATTER.format(transactionObject.getDate().toInstant()),
                transactionObject -> !Objects.isNull(transactionObject.getDate()),
                false);

        addFieldForTrackingListBuilder(
                listBuilder,
                transaction,
                "mutability",
                transactionObject -> transactionObject.getMutability().toString(),
                transactionObject -> !Objects.isNull(transactionObject.getMutability()),
                true);

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
                                                                .PROVIDER_GIVEN_TRANSACTION_ID)),
                true);

        if (!Objects.isNull(transaction.getTransactionAmount())
                && transaction.getTransactionAmount().getDoubleValue() != 0) {
            listBuilder.putRedacted(
                    "transactionAmount", transaction.getTransactionAmount().getDoubleValue());
        } else {
            listBuilder.putNull("transactionAmount");
        }

        for (TransactionDateType type : TransactionDateType.values()) {
            String key = "transactionDate_" + type.toString();
            Optional<AvailableDateInformation> maybeDate =
                    getTransactionDateByType(transaction.getTransactionDates(), type)
                            .map(TransactionDate::getValue);
            if (maybeDate.isPresent()) {
                listBuilder.putRedacted(key, maybeDate.get().toString());
            } else {
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
