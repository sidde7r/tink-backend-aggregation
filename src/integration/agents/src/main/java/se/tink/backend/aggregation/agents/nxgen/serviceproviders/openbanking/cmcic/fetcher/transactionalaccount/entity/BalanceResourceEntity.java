package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceResourceEntity {
    @JsonProperty("name")
    private String name = null;

    @JsonProperty("balanceAmount")
    private AmountTypeEntity balanceAmount = null;

    @JsonProperty("balanceType")
    private BalanceStatusEntity balanceType = null;

    @JsonProperty("lastChangeDateTime")
    private Date lastChangeDateTime = null;

    @JsonProperty("referenceDate")
    private Date referenceDate = null;

    public String getName() {
        return name;
    }

    public AmountTypeEntity getBalanceAmount() {
        return balanceAmount;
    }

    public BalanceStatusEntity getBalanceType() {
        return balanceType;
    }

    public Date getLastChangeDateTime() {
        return lastChangeDateTime;
    }

    public Date getReferenceDate() {
        return referenceDate;
    }
}
