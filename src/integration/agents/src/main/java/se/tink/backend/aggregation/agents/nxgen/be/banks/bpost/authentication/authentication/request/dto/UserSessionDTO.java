package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request.dto;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UserSessionDTO {

    String state;
    boolean isError;
    String sessionId;
    String sessionToken;
}
