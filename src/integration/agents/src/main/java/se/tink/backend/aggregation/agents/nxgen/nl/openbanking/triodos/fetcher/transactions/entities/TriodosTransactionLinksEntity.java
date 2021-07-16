package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.fetcher.transactions.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.TransactionLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TriodosTransactionLinksEntity extends TriodosTransactionsEntity {

    @JsonProperty("_links")
    private TransactionLinksEntity links;

    @JsonIgnore
    public String getNextLink() {
        return links.getNextLink();
    }

    @JsonIgnore
    public boolean hasMore() {
        return Optional.ofNullable(links).map(TransactionLinksEntity::hasNextLink).orElse(false);
    }
}
