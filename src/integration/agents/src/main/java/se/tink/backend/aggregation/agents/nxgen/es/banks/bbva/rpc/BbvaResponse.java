package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.ResultEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BbvaResponse {
    private ResultEntity result;

    public ResultEntity getResult() {
        return result;
    }

    @JsonIgnore
    public boolean hasError() {
        return result != null && result.hasError();
    }

    @JsonIgnore
    public boolean hasError(BbvaConstants.Error errorToFind) {
        return result != null && result.hasError(errorToFind);
    }
}
