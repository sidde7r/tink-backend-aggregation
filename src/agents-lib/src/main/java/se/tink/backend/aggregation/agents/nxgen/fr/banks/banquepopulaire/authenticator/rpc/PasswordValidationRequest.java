package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.rpc;

import com.google.common.collect.Lists;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.entities.PasswordCredentials;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.entities.PasswordCredentialsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PasswordValidationRequest {
    private PasswordCredentialsEntity validate;

    public PasswordValidationRequest(
            PasswordCredentialsEntity validate) {
        this.validate = validate;
    }

    public static PasswordValidationRequest create(String validationId, String password, String id, String login, String type) {
        PasswordCredentials passwordCredentials = PasswordCredentials.create(password, id, login, type);
        PasswordCredentialsEntity passwordCredentialsEntity = new PasswordCredentialsEntity();
        passwordCredentialsEntity.put(validationId, Lists.newArrayList((passwordCredentials)));

        return new PasswordValidationRequest(passwordCredentialsEntity);
    }
}
