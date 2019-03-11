package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.entities.EeILinkingAndLoginEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinkingLoginRequest {
    @JsonProperty("EE_I_VinculacionyLogin")
    private EeILinkingAndLoginEntity eeILinkingAndLogin;

    public LinkingLoginRequest(EeILinkingAndLoginEntity eeILinkingAndLogin) {
        this.eeILinkingAndLogin = eeILinkingAndLogin;
    }
}
