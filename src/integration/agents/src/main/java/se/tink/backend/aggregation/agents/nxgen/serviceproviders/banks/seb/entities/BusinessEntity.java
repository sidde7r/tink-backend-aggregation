package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BusinessEntity {
    @JsonProperty("KUND_NUMMER")
    private String companyNumber;

    @JsonProperty("KUNDNAMN")
    private String companyName;

    @JsonIgnore
    public String getCompanyNumber() {
        return companyNumber;
    }

    @JsonIgnore
    public String getCompanyName() {
        return companyName;
    }
}
