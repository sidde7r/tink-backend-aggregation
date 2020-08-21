package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.accesstoken;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TokenResponseDto {

    private String accessToken;

    private String tokenType;

    private Long expiresIn;

    private String state;

    private String scope;

    private String refreshToken;
}
