package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ChosenScaMethodEntity {
    private String authenticationMethodId;
    private String authenticationType;
    private String name;

    @JsonIgnore
    public String getAuthenticationMethodId() {
        return authenticationMethodId;
    }
}
