package se.tink.backend.aggregation.agents.banks.sbab.rpc;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.banks.sbab.entities.QueryVariables;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@RequiredArgsConstructor
@AllArgsConstructor
public class QueryRequest {
    final String operationName;
    QueryVariables variables = new QueryVariables(null, null, null);
    final String query;
}
