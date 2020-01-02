package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceEntity {

    private String name;
    private BalanceAmountEntity balanceAmount;
    private String balanceType;
    private String lastChangeDateTime;
    private String referenceDate;
    private String lastCommittedTransaction;

    public String getName() {
        return name;
    }

    public BalanceAmountEntity getBalanceAmount() {
        return balanceAmount;
    }

    public String getBalanceType() {
        return balanceType;
    }

    public LocalDateTime getLastChangeDateTime() {
        return LocalDateTime.parse(lastChangeDateTime);
    }

    public LocalDate getReferenceDate() {
        return LocalDate.parse(referenceDate);
    }

    public String getLastCommittedTransaction() {
        return lastCommittedTransaction;
    }
}
