package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities.LoginInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.rpc.BaseResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.rpc.ResultEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginResponse extends BaseResponse {
    private ResultEntity<LoginInfoEntity> result;

    @JsonIgnore
    public LoginInfoEntity getLoginInfoEntity() {
        return result.getData();
    }
}
