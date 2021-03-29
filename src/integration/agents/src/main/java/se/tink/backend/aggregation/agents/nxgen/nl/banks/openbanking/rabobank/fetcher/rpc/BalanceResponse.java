package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class BalanceResponse {

    @JsonProperty("balances")
    private List<BalancesItem> balances;

    @JsonProperty("account")
    private Account account;

    @Override
    public String toString() {
        return "BalanceResponse{"
                + "balances = '"
                + balances
                + '\''
                + ",account = '"
                + account
                + '\''
                + "}";
    }
}
