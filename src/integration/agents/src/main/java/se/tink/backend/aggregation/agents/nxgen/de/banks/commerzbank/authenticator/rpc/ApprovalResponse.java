package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities.StatusEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.rpc.BaseResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.rpc.ResultEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor
public class ApprovalResponse extends BaseResponse {
    private ResultEntity<StatusEntity> result;

    @JsonIgnore
    public StatusEntity getStatusEntity() {
        return Optional.ofNullable(result).map(ResultEntity::getData).orElse(new StatusEntity());
    }
}
