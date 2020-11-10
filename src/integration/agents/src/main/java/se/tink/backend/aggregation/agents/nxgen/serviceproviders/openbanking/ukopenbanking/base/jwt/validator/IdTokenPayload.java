package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.jwt.validator;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class IdTokenPayload {

    private String atHash;

    private String cHash;

    private String sHash;

    private String acr;

    private String aud;

    private String sub;

    private String openbankingIntentId;

    private Integer authTime;

    private String iss;

    private Long exp;

    private Long iat;

    private String nonce;
}
