package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.entities.ValidationResponseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PasswordValidationResponse {
    private String id;
    private String locale;
    private ValidationResponseEntity response;

    public ValidationResponseEntity getResponse() {
        return response;
    }
}
