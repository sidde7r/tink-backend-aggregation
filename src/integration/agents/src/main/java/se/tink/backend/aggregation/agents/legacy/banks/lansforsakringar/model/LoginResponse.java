package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class LoginResponse {
    private SessionEntity session;
}
