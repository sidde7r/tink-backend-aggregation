package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc;

import java.util.Collections;
import java.util.Map;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ExecuteMethodResponse extends ResponseEntity {

    private Map<String, Object> outputs;

    public Map<String, Object> getOutputs() {
        return outputs != null ? outputs : Collections.emptyMap();
    }
}
