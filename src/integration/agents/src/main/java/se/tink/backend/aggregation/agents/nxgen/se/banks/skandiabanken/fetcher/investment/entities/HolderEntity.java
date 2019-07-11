package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class HolderEntity {
    @JsonProperty("Firstname")
    private String firstName = "";

    @JsonProperty("NationalIdentificationNumber")
    private String nationalIdentificationNumber = "";

    @JsonProperty("EncryptedNationalIdentificationNumber")
    private String encryptedNationalIdentificationNumber = "";

    @JsonProperty("Surname")
    private String surname = "";

    @JsonProperty("Fullname")
    private String fullName = "";

    @JsonIgnore
    public String getFullName() {
        if (Strings.isNullOrEmpty(fullName)) {
            return String.format("%s %s", firstName, surname);
        } else {
            return fullName;
        }
    }
}
