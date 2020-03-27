package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities;

import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor
public class MetaDataEntity {
    private String processContextId;

    public String getProcessContextId() {
        return Optional.ofNullable(processContextId)
                .orElseThrow(
                        () ->
                                new NoSuchElementException(
                                        "Expected processContextId but it was null"));
    }
}
