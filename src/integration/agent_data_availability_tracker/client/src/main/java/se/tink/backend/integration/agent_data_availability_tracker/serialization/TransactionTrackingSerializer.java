package se.tink.backend.integration.agent_data_availability_tracker.serialization;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransactionDate;
import se.tink.backend.aggregation.agents.models.TransactionDateType;
import se.tink.backend.integration.agent_data_availability_tracker.common.serialization.TrackingList;
import se.tink.backend.integration.agent_data_availability_tracker.common.serialization.TrackingMapSerializer;

public class TransactionTrackingSerializer extends TrackingMapSerializer {

    private static final String TRANSACTION_ENTITY_NAME = "Transaction";
    private final Transaction transaction;

    public TransactionTrackingSerializer(Transaction transaction) {
        /*
           TODO (AAP-1039): We should also add the type of the account from where this transaction
           comes from. In this case we need to get another parameter which will contain
           this information
        */
        super(String.format(TRANSACTION_ENTITY_NAME + "<%s>", ""));
        this.transaction = transaction;
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

        if (!Objects.isNull(transaction.getDate())) {
            listBuilder.putRedacted("date", transaction.getDate());
        } else {
            listBuilder.putNull("date");
        }

        if (!Objects.isNull(transaction.getTransactionAmount())
                && transaction.getTransactionAmount().getDoubleValue() != 0) {
            listBuilder.putRedacted(
                    "transactionAmount", transaction.getTransactionAmount().getDoubleValue());
        } else {
            listBuilder.putNull("transactionAmount");
        }

        for (TransactionDateType type : TransactionDateType.values()) {
            String key = "transactionDate_" + type.toString();
            Optional<TransactionDate> transactionDate =
                    getTransactionDateByType(transaction.getTransactionDates(), type);

            if (!transactionDate.isPresent()) {
                listBuilder.putNull(key);
                continue;
            }

            listBuilder.putRedacted(key, transactionDate.get().getValue().getDate().toString());
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
