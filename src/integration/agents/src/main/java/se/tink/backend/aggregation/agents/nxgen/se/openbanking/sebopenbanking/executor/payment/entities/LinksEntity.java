package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    @JsonProperty("startAuthorisationWithAuthenticationMethodSelection")
    private AuthenticationMethodSelectionEntity methodSelection;

    @JsonIgnore
    public boolean hasMethodSelectionEntity() {
        return methodSelection != null && methodSelection.hasMethodSelectionEntity();
    }
}
