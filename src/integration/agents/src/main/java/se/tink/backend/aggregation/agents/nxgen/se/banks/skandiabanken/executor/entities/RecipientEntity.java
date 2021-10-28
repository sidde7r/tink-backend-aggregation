package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
@Getter
public class RecipientEntity {
    private String encryptedRecipientId;
    private String giroNumber;
    private String name;
}
