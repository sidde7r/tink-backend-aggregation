package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.entity.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.entity.consent.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountLinksEntity {
    @JsonProperty("balances")
    private LinkEntity balanceUrl;

    @JsonProperty("transactions")
    private LinkEntity transactionUrl;

    public String getBalanceUrl() {
        return balanceUrl.getUrl();
    }

    public String getTransactionUrl() {
        return transactionUrl.getUrl();
    }
}
