package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.authenticator;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class TokenResponse {

    private String tokenType;

    private String accessToken;

    private Long expiresIn;

    private String refreshToken;
}
