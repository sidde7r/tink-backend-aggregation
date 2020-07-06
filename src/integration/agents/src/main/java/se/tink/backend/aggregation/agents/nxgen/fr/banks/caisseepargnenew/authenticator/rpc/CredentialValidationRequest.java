package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.entities.ValidationUnit;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Builder
public class CredentialValidationRequest {
    @JsonProperty("validate")
    private Map<String, List<ValidationUnit>> validate;
}
