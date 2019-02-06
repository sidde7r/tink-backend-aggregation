package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.entity.DataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ValidateSubscriptionResponse {
    private DataEntity data;

    public DataEntity getData() {
        return data;
    }
}
