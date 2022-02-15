package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.payment.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
@RequiredArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class PispTokenRequest {

    private static final String PISP_GRAN_TYPE = "client_credentials";
    private static final String PISP_SCOPE = "pisp";

    private final String grantType = PISP_GRAN_TYPE;
    private final String scope = PISP_SCOPE;
    private final String clientId;
}
