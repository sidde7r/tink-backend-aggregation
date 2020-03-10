package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator;

import static java.lang.String.format;

import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.experimental.Accessors;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@Accessors(chain = true)
@JsonObject
@JsonNaming(SnakeCaseStrategy.class)
class AccessTokenResponse {

    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private String scope;

    String getValue() {
        return format("%s %s", tokenType, accessToken);
    }
}
