package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.validator;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
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

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<String> aud;

    private String sub;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<String> openbankingIntentId;

    private Integer authTime;

    private String iss;

    private Long exp;

    private Long iat;

    private String nonce;
}
