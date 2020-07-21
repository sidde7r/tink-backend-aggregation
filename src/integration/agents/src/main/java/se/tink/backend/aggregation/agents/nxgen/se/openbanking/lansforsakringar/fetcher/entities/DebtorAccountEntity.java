package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DebtorAccountEntity {

    private String bban;
    private String currency;

    @JsonIgnore
    public String getBban() {
        return bban;
    }

    @JsonIgnore
    public String getCurrency() {
        return currency;
    }
}
