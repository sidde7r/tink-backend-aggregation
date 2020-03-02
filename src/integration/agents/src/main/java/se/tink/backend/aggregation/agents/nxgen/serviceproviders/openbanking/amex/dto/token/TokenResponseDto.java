package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.token;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class TokenResponseDto {

    private String accessToken;

    private String refreshToken;

    private Long expiresIn;

    private String scope;

    private String tokenType;

    private String macKey;

    private String macAlgorithm;
}
