package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.entities.LinkListEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsResponseEntity {
    @JsonProperty("_continuationKey")
    private String continuationKey;
    private List<TransactionEntity> transactions;
    @JsonProperty("_links")
    private LinkListEntity links;

    public String getContinuationKey() {
        return continuationKey;
    }

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    @JsonIgnore
    public Optional<LinkEntity> findLinkByName(String name) {
        return links.findLinkByName(name);
    }
}
