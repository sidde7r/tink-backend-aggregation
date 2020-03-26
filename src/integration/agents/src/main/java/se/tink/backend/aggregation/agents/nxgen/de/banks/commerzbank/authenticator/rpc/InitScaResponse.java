package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities.InitScaEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities.ErrorEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities.MetaDataEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.rpc.BaseResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.rpc.ResultEntity;

@AllArgsConstructor
@NoArgsConstructor
public class InitScaResponse extends BaseResponse {
    private ResultEntity<InitScaEntity> result;

    public InitScaResponse(
            ResultEntity<InitScaEntity> result, ErrorEntity error, MetaDataEntity metaData) {
        super(error, metaData);
        this.result = result;
    }

    @JsonIgnore
    public InitScaEntity getInitScaEntity() {
        return result.getData();
    }
}
