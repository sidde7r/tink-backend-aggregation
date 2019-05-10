package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class HolderEntity {
    @JsonProperty("EncryptedNationalIdentificationNumber")
    private String encryptedNationalIdentificationNumber = "";

    @JsonProperty("Firstname")
    private String firstname = "";

    @JsonProperty("NationalIdentificationNumber")
    private String nationalIdentificationNumber = "";

    @JsonProperty("Surname")
    private String surname = "";

    @JsonIgnore
    public String getHolderName() {
        return String.format("%s %s", firstname, surname);
    }
}
