package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.authenticator.entities;

import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "unused"})
public class ScaMethodEntity {

    private String authenticationType;
    private String authenticationMethodId;
    private String name;
    private String explanation;

    public String getAuthenticationMethodId() {
        return authenticationMethodId;
    }

    /* 1. display option for SCA method
       2. remove "_" in option "NO_PREFERENCE"
    */
    public String toString() {
        return StringUtils.capitalize(authenticationType.replace("_", " ").toLowerCase());
    }
}
