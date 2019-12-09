package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksTransactionsEntity {

    @JsonProperty("next")
    private HrefEntity nextTransactionEntity;

    private HrefEntity balances;

    private HrefEntity lastEntity;

    private HrefEntity prevEntity;

    private HrefEntity selfTransactionsEntity;

    private HrefEntity firstEntity;

    @JsonProperty("parent-list")
    private HrefEntity hrefEntity;

    public HrefEntity getNextTransactionEntity() {
        return nextTransactionEntity;
    }
}
