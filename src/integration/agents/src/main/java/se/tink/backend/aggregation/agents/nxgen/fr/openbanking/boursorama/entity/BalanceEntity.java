package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity;

import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceEntity {

    private String name;
    private BalanceAmountEntity balanceAmount;
    private String balanceType;
    private Date lastChangeDateTime;
    private Date referenceDate;
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

    public Date getLastChangeDateTime() {
        return lastChangeDateTime;
    }

    public Date getReferenceDate() {
        return referenceDate;
    }

    public String getLastCommittedTransaction() {
        return lastCommittedTransaction;
    }
}
