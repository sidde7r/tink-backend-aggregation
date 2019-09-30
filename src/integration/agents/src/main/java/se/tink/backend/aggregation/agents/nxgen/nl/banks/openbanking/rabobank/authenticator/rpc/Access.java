package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc.Account;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Access {

    @JsonProperty("ais.balances.read")
    private List<Account> aisBalanceRead;

    @JsonProperty("ais.transactions.read-history")
    private List<Account> aisTransactionsReadHistory;

    @JsonProperty("ais.transactions.read-90days")
    private List<Account> aisTransactionsRead90Days;
}
