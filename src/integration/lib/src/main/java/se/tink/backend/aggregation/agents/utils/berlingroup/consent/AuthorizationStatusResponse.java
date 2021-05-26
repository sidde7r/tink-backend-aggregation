package se.tink.backend.aggregation.agents.utils.berlingroup.consent;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AuthorizationStatusResponse {
    private String scaStatus;
}
