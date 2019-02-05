package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.authenticator.entites.NumpadDataEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NumpadResponse extends BaseResponse {
    private NumpadDataEntity data;

    public NumpadDataEntity getData() {
        return data;
    }
}
