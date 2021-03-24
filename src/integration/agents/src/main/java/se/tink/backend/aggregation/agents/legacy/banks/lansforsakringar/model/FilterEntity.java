package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
public class FilterEntity {
    private IncludeFilterEntity includeFilter;

    public static FilterEntity of(String transactionStatus) {
        IncludeFilterEntity filterEntity = new IncludeFilterEntity(transactionStatus);
        return new FilterEntity(filterEntity);
    }
}
