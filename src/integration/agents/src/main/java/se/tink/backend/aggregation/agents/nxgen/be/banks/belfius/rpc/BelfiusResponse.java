package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BelfiusResponse {
    protected boolean pendingResponseSets;
    protected List<ResponseSet> responseSets;

    public boolean hasPendingResponseSets() {
        return pendingResponseSets;
    }

    public List<ResponseSet> getResponseSets() {
        return responseSets != null ? responseSets : Collections.emptyList();
    }

    public <T extends ResponseEntity> Stream<T> filter(Class<T> c) {
        return responseSets.stream()
                .flatMap(responseSet -> responseSet.getResponses().stream())
                .filter(r -> r != null && c.isAssignableFrom(r.getClass()))
                .map(r -> (T) r);
    }
}
