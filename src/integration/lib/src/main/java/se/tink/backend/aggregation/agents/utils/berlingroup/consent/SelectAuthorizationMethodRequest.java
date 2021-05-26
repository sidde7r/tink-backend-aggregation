package se.tink.backend.aggregation.agents.utils.berlingroup.consent;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
public class SelectAuthorizationMethodRequest {
    private String authenticationMethodId;
}
