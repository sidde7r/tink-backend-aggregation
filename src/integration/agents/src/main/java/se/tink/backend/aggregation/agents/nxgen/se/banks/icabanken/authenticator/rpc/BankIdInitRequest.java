package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy.UpperCamelCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
@JsonNaming(UpperCamelCaseStrategy.class)
public class BankIdInitRequest {
    private String personalIdentityNumber;
}
