package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class IdentifiersDto {

    private String displayAccountNumber;

    private String accountKey;

    private boolean isBasic;
}
