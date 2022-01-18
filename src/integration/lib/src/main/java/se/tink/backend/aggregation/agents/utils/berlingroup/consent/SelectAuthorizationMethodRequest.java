package se.tink.backend.aggregation.agents.utils.berlingroup.consent;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
@EqualsAndHashCode
public class SelectAuthorizationMethodRequest {
    private String authenticationMethodId;
}
