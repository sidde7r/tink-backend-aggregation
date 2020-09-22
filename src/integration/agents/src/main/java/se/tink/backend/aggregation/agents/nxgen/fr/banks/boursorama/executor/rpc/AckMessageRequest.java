package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.executor.rpc;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
public class AckMessageRequest {
    private String resourceId;
}
