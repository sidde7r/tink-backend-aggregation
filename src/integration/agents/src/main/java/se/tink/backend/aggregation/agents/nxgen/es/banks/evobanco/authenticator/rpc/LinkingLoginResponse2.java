package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.entities.EeOLinkingAndLoginEntity2;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinkingLoginResponse2 {
    @JsonProperty("EE_O_VinculacionyLogin")
    private EeOLinkingAndLoginEntity2 eeOLinkingAndLogin;

    public EeOLinkingAndLoginEntity2 getEeOLinkingAndLogin() {
        return eeOLinkingAndLogin;
    }
}
