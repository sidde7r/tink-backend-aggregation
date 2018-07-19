package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionRequestBody {
    private SearchCriteriaDto searchCriteriaDto;
    private Identifier identifier;

    public TransactionRequestBody(SearchCriteriaDto searchCriteriaDto, Identifier identifier) {
        this.searchCriteriaDto = searchCriteriaDto;
        this.identifier = identifier;
    }
}
