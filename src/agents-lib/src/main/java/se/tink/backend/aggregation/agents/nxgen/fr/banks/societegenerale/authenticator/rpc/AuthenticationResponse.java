package se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.entities.CommonEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.authenticator.entities.DataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticationResponse {

    @JsonProperty("commun")
    private CommonEntity common;
    @JsonProperty("donnees")
    private DataEntity data;

    public CommonEntity getCommon() {
        return common;
    }

    public DataEntity getData() {
        return data;
    }

}
