package se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.authenticator.entiries.LoginGridData;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.entities.MetaData;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginGridResponse {
    @JsonProperty("commun")
    private MetaData metaData;
    @JsonProperty("donnees")
    private LoginGridData data;

    public LoginGridData getData() {
        return data;
    }
}
