package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.entity;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class CheckPinAutoResponsePayload {
    private String accessToken;
}
