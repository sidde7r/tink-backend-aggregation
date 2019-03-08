package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.entities.ResultsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CompleteTransferResponse {
    @JsonObject private List<ResultsEntity> results;
}
