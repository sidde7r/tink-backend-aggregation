package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.creditcard.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class ExpirationEntity {
    private String expireDate;
    private int expireStatus;
}
