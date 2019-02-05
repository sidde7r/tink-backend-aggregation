package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.entities.StepEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.entities.ValidationUnit;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitiateSessionResponse {
    private String id;
    private String locale;
    private StepEntity step;

    public String getId() {
        return id;
    }
    @JsonIgnore
    public HashMap<String, List<ValidationUnit>> getFirstValidationUnit() {
        return step.getValidationUnits().stream()
                .findFirst().orElseThrow(() -> new IllegalStateException("Could not find validation unit"));
    }
}
