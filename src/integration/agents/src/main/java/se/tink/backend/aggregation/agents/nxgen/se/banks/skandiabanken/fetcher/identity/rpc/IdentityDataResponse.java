package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.identity.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IdentityDataResponse {

    @JsonProperty("Fullname")
    private String fullname = "";

    @JsonProperty("Firstname")
    private String firstname = "";

    @JsonProperty("Surname")
    private String surname = "";

    @JsonProperty("NationalIdentificationNumber")
    private String nationalIdentificationNumber = "";

    @JsonIgnore
    public String getFullname() {
        return fullname;
    }

    @JsonIgnore
    public String getFirstname() {
        return firstname;
    }

    @JsonIgnore
    public String getSurname() {
        return surname;
    }

    @JsonIgnore
    public String getNationalIdentificationNumber() {
        return nationalIdentificationNumber;
    }
}
