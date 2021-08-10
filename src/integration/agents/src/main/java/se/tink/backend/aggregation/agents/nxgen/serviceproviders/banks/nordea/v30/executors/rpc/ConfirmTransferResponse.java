package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.entities.ErrorEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConfirmTransferResponse {
    @JsonProperty private List<String> results;
    @JsonProperty private List<ErrorEntity> errors;

    @JsonIgnore
    public String getResult() {
        return results.get(0);
    }
}
