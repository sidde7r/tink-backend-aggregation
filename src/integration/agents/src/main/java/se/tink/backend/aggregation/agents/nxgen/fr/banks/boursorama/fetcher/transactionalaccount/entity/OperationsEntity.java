package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.fetcher.transactionalaccount.entity;

import java.time.LocalDate;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class OperationsEntity {

    private double amount;
    private String currency;
    private List<DatesEntity> dates;
    private List<LabelsEntity> labels;

    public Transaction toTinkTransaction() {
        final String description = getDescription();
        return Transaction.builder()
                .setDescription(description)
                .setDate(getPerformedDate())
                .setAmount(getTinkAmount())
                .setType(getType(description))
                .build();
    }

    private ExactCurrencyAmount getTinkAmount() {
        return ExactCurrencyAmount.of(amount, currency);
    }

    private LocalDate getPerformedDate() {
        return dates.stream()
                .filter(DatesEntity::isPerformedOnDate)
                .map(DatesEntity::getDate)
                .findFirst()
                .orElse(null);
    }

    private String getDescription() {
        return labels.stream()
                .filter(LabelsEntity::isTransactionDescriptionLabel)
                .map(LabelsEntity::getBody)
                .findFirst()
                .orElse(null);
    }

    private static TransactionTypes getType(String description) {
        if (StringUtils.isBlank(description)) {
            return TransactionTypes.DEFAULT;
        }

        if (description.startsWith("VIR")) {
            return TransactionTypes.TRANSFER;
        }

        if (description.startsWith("PRLV")) {
            return TransactionTypes.PAYMENT;
        }

        if (description.startsWith("Relev")) {
            return TransactionTypes.CREDIT_CARD;
        }

        if (description.startsWith("RETRAIT")) {
            return TransactionTypes.WITHDRAWAL;
        }

        return TransactionTypes.DEFAULT;
    }
}
