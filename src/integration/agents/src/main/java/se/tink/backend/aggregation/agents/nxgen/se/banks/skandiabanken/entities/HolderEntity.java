package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class HolderEntity {
    private String encryptedNationalIdentificationNumber = "";
    private String firstname = "";
    private String nationalIdentificationNumber = "";
    private String surname = "";

    @JsonIgnore
    public String getHolderName() {
        return String.format("%s %s", firstname, surname);
    }
}
