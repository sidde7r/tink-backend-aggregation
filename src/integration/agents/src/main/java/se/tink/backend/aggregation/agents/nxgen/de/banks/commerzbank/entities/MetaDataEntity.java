package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities;

import java.util.NoSuchElementException;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MetaDataEntity {
    private String globalRequestId;
    private String processContextId;

    public String getGlobalRequestId() {
        return globalRequestId;
    }

    public String getProcessContextId() {
        return Optional.ofNullable(processContextId)
                .orElseThrow(
                        () ->
                                new NoSuchElementException(
                                        "Expected processContextId but it was null"));
    }
}
