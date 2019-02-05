package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public enum InvalidateReasonEntity {
    @JsonProperty("user_sign_out")
    USER_SIGN_OUT,
    @JsonProperty("system_sign_out")
    SYSTEM_SIGN_OUT,
    @JsonProperty("token_time_out")
    TOKEN_TIME_OUT,
}
