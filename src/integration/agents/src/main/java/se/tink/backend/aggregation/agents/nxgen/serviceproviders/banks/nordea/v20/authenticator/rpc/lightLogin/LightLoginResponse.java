package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.authenticator.rpc.lightLogin;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.authenticator.entities.lightLogin.AuthenticationServiceFaultEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.authenticator.entities.lightLogin.LightLoginEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.rpc.NordeaResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LightLoginResponse extends NordeaResponse {
    @JsonProperty("lightLoginResponse")
    private LightLoginEntity lightLoginEntity;

    private AuthenticationServiceFaultEntity authenticationServiceFault;

    public LightLoginEntity getLightLoginEntity() {
        return lightLoginEntity;
    }

    public AuthenticationServiceFaultEntity getAuthenticationServiceFault() {
        return authenticationServiceFault;
    }

    @Override
    public Optional<String> getErrorCode() {
        Optional<String> errorCode = Optional.empty();

        if (authenticationServiceFault != null) {
            errorCode = authenticationServiceFault.getErrorCode();
        } else if (lightLoginEntity != null) {
            errorCode = lightLoginEntity.getErrorCode();
        }

        // If "authenticationServiceFault" or "lightLoginEntity" doesn't contain any errors, check
        // for default errors
        return errorCode.isPresent() ? errorCode : super.getErrorCode();
    }

    public Optional<String> getToken() {
        return Optional.ofNullable(lightLoginEntity).map(LightLoginEntity::getToken);
    }
}
