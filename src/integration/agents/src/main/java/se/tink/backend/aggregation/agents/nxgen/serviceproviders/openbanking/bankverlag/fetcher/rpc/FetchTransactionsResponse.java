package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.fetcher.entities.TransactionsLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class FetchTransactionsResponse {
    private AccountEntity account;

    @JsonProperty("_links")
    private TransactionsLinksEntity links;
}
