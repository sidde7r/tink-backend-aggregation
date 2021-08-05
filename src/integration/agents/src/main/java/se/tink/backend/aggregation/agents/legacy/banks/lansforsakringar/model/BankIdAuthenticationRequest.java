package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@RequiredArgsConstructor
public class BankIdAuthenticationRequest {
    private final String ip = "::1";
    private final String userId;
}
