package se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.authenticator.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
@Getter
public class LoginStatusResponse {

    private boolean isAuthenticated;
    private String version;
}
