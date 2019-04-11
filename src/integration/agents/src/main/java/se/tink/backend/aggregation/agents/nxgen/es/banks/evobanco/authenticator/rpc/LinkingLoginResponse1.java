package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.entities.EeOLinkingAndLoginEntity1;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.entities.ErrorsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.rpc.EERpcResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinkingLoginResponse1 extends EEBaseLoginResponse implements EERpcResponse {
    @JsonProperty("EE_O_VinculacionyLogin")
    private EeOLinkingAndLoginEntity1 eeOLinkingAndLogin;

    public EeOLinkingAndLoginEntity1 getEeOLinkingAndLogin() {
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
