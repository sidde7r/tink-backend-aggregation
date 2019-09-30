package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities.InitScaEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.rpc.BaseResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.rpc.ResultEntity;

public class InitScaResponse extends BaseResponse {
    private ResultEntity<InitScaEntity> result;

    @JsonIgnore
    public InitScaEntity getInitScaEntity() {
        return result.getData();
    }
}
