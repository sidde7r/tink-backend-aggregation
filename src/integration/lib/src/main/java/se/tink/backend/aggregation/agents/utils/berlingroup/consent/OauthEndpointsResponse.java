package se.tink.backend.aggregation.agents.utils.berlingroup.consent;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
public class OauthEndpointsResponse {
    private String authorizationEndpoint;
    private String tokenEndpoint;
}
