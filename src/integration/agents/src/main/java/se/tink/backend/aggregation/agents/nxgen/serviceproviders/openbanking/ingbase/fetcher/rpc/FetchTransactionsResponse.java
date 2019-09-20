package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.entities.TransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchTransactionsResponse {

    private TransactionsEntity transactions;

    private AccountEntity account;

    @JsonProperty("_links")
    private LinkEntity links;

    public TransactionsEntity getTransactions() {
        return transactions;
    }

    public String getNextLink() {
        return Optional.ofNullable(links)
                .map(LinkEntity::getHref)
                .orElse(
                        Optional.ofNullable(transactions)
                                .map(TransactionsEntity::getNextLink)
                                .orElse(null));
    }
}
