package se.tink.backend.aggregation.agents.banks.sbab.entities;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@RequiredArgsConstructor
public class QueryVariables {
    final QueryFilter filter;
    final String accountNumber;
    final String loanNumber;
}
