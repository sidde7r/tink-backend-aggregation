package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BalanceEntity {
    @JsonProperty private String balanceType;
    @JsonProperty private AmountEntity balanceAmount;
    @JsonProperty private boolean creditLimitIncluded;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    // actual format includes microseconds, docs say it's ms
    private Date lastChangeDateTime;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "UTC")
    private Date referenceDate;

    @JsonIgnore
    public Amount getAmount() {
        return balanceAmount.toTinkAmount();
    }

    public Date getLastChangeDateTime() {
        return lastChangeDateTime;
    }

    public Date getReferenceDate() {
        return referenceDate;
    }

    public String getBalanceType() {
        return balanceType;
    }
}
