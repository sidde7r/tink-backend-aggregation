package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountEntity {
    @JsonProperty("_type")
    private String type;

    private String currency;
    private String value;

    public AccountEntity() {}

    public AccountEntity(String type, String currency, String value) {
        this.type = type;
        this.currency = currency;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public String getCurrency() {
        return currency;
    }

    public String getValue() {
        return value;
    }
}
