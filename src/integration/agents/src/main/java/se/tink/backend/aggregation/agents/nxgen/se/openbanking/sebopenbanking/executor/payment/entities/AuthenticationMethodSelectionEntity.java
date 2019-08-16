package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticationMethodSelectionEntity {
    String href;

    @JsonIgnore
    public boolean hasMethodSelectionEntity() {
        return href != null;
    }
}
