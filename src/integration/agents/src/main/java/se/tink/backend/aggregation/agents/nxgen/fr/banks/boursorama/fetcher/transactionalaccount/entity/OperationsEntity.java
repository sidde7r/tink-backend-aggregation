package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.fetcher.transactionalaccount.entity;

import java.time.LocalDate;
import java.util.List;
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
        return Transaction.builder()
                .setDescription(getDescription())
                .setDate(getPerformedDate())
                .setAmount(getTinkAmount())
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
}
