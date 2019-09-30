package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities.StatusEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.rpc.BaseResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.rpc.ResultEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ApprovalResponse extends BaseResponse {
    private ResultEntity<StatusEntity> result;

    @JsonIgnore
    public StatusEntity getStatusEntity() {
        return result.getData();
    }
}
