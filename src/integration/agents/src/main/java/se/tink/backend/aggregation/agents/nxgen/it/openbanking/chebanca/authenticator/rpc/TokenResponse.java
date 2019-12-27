package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.entities.DataEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.entities.ResultEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@JsonObject
public class TokenResponse {
    @JsonProperty("data")
    private DataEntity data;

    @JsonProperty("result")
    private ResultEntity result;

    @JsonIgnore
    public OAuth2Token toTinkToken() {
        return data.toTinkToken();
    }
}
