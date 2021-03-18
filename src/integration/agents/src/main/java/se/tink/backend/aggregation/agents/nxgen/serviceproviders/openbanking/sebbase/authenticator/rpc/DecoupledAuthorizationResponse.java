package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class DecoupledAuthorizationResponse {
    private String authReqId;
    private String autoStartToken;
    private String hintCode;
    private String status;
    private int pollDelay;
}
