package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class PaymentCompleteEntity {

    private String description;
    private String statusDisplayName;
}
