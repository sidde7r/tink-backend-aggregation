package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.entities.EeOLinkingAndLoginEntity2;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.entities.ErrorsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.rpc.EERpcResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.Optional;

@JsonObject
public class LinkingLoginResponse2 extends EEBaseLoginResponse implements EERpcResponse {
    @JsonProperty("EE_O_VinculacionyLogin")
    private EeOLinkingAndLoginEntity2 eeOLinkingAndLogin;

    public EeOLinkingAndLoginEntity2 getEeOLinkingAndLogin() {
        return eeOLinkingAndLogin;
    }

    @Override
    public boolean isUnsuccessfulReturnCode() {
        return eeOLinkingAndLogin.isUnsuccessfulReturnCode();
    }

    @Override
    public Optional<ErrorsEntity> getErrors() {
       return eeOLinkingAndLogin.getErrors();
    }
}
