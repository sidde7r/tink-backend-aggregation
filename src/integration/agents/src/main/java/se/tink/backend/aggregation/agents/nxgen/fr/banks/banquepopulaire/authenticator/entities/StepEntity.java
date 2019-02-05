package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class StepEntity {
    private PhaseEntity phase;
    private List<ValidationUnitsEntity> validationUnits;

    public List<ValidationUnitsEntity> getValidationUnits() {
        return validationUnits;
    }
}


