package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.authenticator.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ScaMethod {

    // DECOUPLED
    private String scaApproach;

    // SMART_ID, MOBILE_ID, CODE_CALCULATOR, IDCARD
    private String authenticationMethodId;

    private String explanation;
}
