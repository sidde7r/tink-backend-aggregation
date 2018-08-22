package se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.authenticator.entities.ChattenivouthEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.entities.CommonEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthInfoResponse {

    @JsonProperty("commun")
    private CommonEntity common;

    @JsonProperty("chgtnivauth")
    private ChattenivouthEntity chattenivouth;

    public CommonEntity getCommon() {
        return common;
    }

}