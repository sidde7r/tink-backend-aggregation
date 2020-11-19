package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import se.tink.backend.aggregation.annotations.JsonObject;

@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
@EqualsAndHashCode
@JsonObject
public class CardDetailsRequest {
    private String cardId;
}
