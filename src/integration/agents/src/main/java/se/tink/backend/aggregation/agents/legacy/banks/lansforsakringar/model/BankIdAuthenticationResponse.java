package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class BankIdAuthenticationResponse {
    private String autoStartToken;
    private String orderRef;
}
