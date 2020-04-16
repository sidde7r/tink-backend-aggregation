package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class ConsentAuthorization {

    private String scaStatus;
    private String authorisationId;
    private List<ScaMethod> scaMethods = new ArrayList<>();

    boolean isScaMethodSelectionRequired() {
        return !getScaMethods().isEmpty();
    }

    @Data
    @JsonObject
    @Accessors(chain = true)
    static class ScaMethod implements SelectableMethod {
        private String name;

        @JsonProperty("authenticationMethodId")
        private String identifier;
    }
}
