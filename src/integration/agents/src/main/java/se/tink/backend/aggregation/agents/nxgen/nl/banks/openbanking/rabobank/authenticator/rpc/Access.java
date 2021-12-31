package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.ToString;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc.AccountConsent;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@ToString
public class Access {

    @JsonProperty("ais.balances.read")
    private List<AccountConsent> aisBalanceRead;

    @JsonProperty("ais.transactions.read-history")
    private List<AccountConsent> aisTransactionsReadHistory;

    @JsonProperty("ais.transactions.read-90days")
    private List<AccountConsent> aisTransactionsRead90Days;
}
