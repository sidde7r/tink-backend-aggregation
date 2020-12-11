package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class CardEntity {

    private String typeName;

    @JsonIgnore
    public boolean isDebitCard() {
        return "BankCard".equalsIgnoreCase(typeName) || "ElectronCard".equalsIgnoreCase(typeName);
    }
}
