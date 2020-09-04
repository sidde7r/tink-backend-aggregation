package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy.UpperCamelCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(UpperCamelCaseStrategy.class)
@Getter
public class BankIdAuthPollBodyEntity {
    private String orderRef;
    private String orderStatus;
}
