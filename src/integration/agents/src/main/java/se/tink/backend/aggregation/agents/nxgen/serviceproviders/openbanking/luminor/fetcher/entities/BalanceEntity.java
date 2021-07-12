package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class BalanceEntity {

    @JsonProperty("balanceAmount")
    BalanceAmountEntity balanceAmountEntity;

    String balanceType;
    boolean creditLimitIncluded;

    public boolean isAvailableBalance() {
        return balanceType.equalsIgnoreCase("interimAvailable");
    }

    public boolean isCreditLimitIncluded() {
        return creditLimitIncluded;
    }
}
