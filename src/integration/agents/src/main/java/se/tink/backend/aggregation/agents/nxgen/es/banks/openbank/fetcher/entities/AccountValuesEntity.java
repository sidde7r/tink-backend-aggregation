package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountValuesEntity {
    @JsonProperty("numerodecuenta")
    private String accountNumber;

    @JsonProperty("digitodecontrol")
    private String checkDigits;

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getCheckDigits() {
        return checkDigits;
    }
}
