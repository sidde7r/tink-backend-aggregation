package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities.PrepareApprovalEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.rpc.BaseResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.rpc.ResultEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PrepareApprovalResponse extends BaseResponse {
    private ResultEntity<PrepareApprovalEntity> result;

    @JsonIgnore
    public PrepareApprovalEntity getPrepareApprovalEntity() {
        return result.getData();
    }
}
