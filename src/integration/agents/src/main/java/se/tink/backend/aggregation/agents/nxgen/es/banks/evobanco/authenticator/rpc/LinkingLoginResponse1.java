package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.entities.EeOLinkingAndLoginEntity1;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinkingLoginResponse1 {
    @JsonProperty("EE_O_VinculacionyLogin")
    private EeOLinkingAndLoginEntity1 eeOLinkingAndLogin;

    public EeOLinkingAndLoginEntity1 getEeOLinkingAndLogin() {
        return eeOLinkingAndLogin;
    }
}
