package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.rpc;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
public class TransactionRequestBody {

    private final SearchCriteriaDto searchCriteriaDto;
    private final Identifier identifier;
}
