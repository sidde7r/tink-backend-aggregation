package se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.entities.FetcherLinksEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.entities.TransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class FetchTransactionsResponse {
    private AccountEntity account;

    private TransactionsEntity transactions;

    @JsonProperty("_links")
    private FetcherLinksEntity links;
}
