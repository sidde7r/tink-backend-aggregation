package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public class ClaimsQueryParamData {

    @JsonProperty("id_token")
    private IdToken idToken;

    @JsonProperty("userinfo")
    private Userinfo userinfo;
}
