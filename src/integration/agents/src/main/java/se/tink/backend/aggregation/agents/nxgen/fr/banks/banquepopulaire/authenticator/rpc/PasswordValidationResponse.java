package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.entities.PhaseEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.entities.ValidationResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.entities.ValidationUnitsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PasswordValidationResponse {
    private String id;
    private String locale;
    private ValidationResponseEntity response;
    private PhaseEntity phase;
    private List<ValidationUnitsEntity> validationUnits;

    public ValidationResponseEntity getResponse() {
        return response;
    }

    public String getValidationStatus() {
        if (response != null) {
            return response.getStatus();
        } else if (phase != null) {
            return phase.getPreviousResult();
        }

        return "";
    }
}
