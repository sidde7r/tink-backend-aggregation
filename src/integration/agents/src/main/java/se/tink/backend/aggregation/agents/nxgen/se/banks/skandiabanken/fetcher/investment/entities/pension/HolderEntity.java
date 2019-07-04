package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities.pension;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class HolderEntity {

    @JsonProperty("Fullname")
    private String fullname = "";

    @JsonProperty("NationalIdentificationNumber")
    private String nationalIdentificationNumber = "";

    @JsonProperty("Firstname")
    private String firstname = "";

    @JsonProperty("Surname")
    private String surname = "";

    @JsonIgnore
    public String getFullname() {
        return fullname;
    }
}
