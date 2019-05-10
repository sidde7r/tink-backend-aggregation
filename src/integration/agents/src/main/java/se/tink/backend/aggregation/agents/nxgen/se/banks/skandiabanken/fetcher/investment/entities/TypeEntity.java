package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TypeEntity {
    @JsonProperty("AccountTypeName")
    private String accountTypeName = "";

    @JsonProperty("AccountTypeId")
    private int accountTypeId;

    @JsonIgnore
    public String getAccountTypeName() {
        return accountTypeName;
    }
}
