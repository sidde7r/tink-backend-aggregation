package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.upcomingtransaction.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
class BankAccountEntity {
    @JsonProperty("BankAccountDisplayName")
    private String bankAccountDisplayName;

    @JsonProperty("BankAccountNumber")
    private String bankAccountNumber;

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }
}
