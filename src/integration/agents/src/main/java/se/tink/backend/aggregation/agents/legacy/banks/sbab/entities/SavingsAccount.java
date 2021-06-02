package se.tink.backend.aggregation.agents.banks.sbab.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonObject
public class SavingsAccount {
    String id;
    String name;
    String description;
    BigDecimal balance;
    BigDecimal availableForWithdrawal;
    String status;
    String accountType;
    BigDecimal interestRate;

    @JsonProperty("__typename")
    String typeName;

    public String getId() {
        return id;
    }
}
