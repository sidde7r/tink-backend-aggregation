package se.tink.backend.aggregation.agents.banks.sbab.entities;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@RequiredArgsConstructor
public class QueryFilter {
    final String statusExcept;
}
