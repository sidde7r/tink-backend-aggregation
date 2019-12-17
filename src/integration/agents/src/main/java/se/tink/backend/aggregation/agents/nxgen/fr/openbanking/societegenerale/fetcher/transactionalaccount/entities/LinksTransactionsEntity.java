package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksTransactionsEntity {

    @JsonProperty("next")
    private Href nextTransactionEntity;

    private Href balances;

    private Href lastEntity;

    private Href prevEntity;

    private Href selfTransactionsEntity;

    private Href firstEntity;

    @JsonProperty("parent-list")
    private Href hrefEntity;

    public Href getNextTransactionEntity() {
        return nextTransactionEntity;
    }
}
