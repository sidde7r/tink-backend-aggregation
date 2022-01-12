package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.authenticator;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ArkeaTokenResponse {

    private String tokenType;
    private String accessToken;
    private Long expiresIn;
    private String refreshToken;
}
