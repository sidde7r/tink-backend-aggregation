package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public enum AuthMethodEntity {
    @JsonProperty("desktop_bankid")
    DESKTOP_BANKID,
    @JsonProperty("mobile_bankid")
    MOBILE_BANKID,
}
