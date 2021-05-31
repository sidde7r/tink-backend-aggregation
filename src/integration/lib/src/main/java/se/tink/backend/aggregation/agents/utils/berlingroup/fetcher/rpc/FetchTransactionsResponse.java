package se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.entities.TransactionsLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class FetchTransactionsResponse {
    private AccountEntity account;

    @JsonProperty("_links")
    private TransactionsLinksEntity links;
}
