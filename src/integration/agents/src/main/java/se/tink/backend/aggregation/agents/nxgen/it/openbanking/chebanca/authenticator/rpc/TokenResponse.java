package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.entities.DataEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.entities.ResultEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TokenResponse {
    @JsonProperty("data")
    private DataEntity data;

    @JsonProperty("result")
    private ResultEntity result;

    public DataEntity getData() {
        return data;
    }

    public ResultEntity getResult() {
        return result;
    }
}
