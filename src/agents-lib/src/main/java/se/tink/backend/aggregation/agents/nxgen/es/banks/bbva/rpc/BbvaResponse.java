package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.rpc;

import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.ResultEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BbvaResponse {
    private ResultEntity result;

    public ResultEntity getResult() {
        return result;
    }
}
