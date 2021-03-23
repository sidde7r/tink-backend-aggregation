package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
public class PagerEntity {
    private int size;
    private int page;
}
