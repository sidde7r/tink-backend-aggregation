package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalancesItem {

    @JsonProperty("balanceType")
    private String balanceType;

    @JsonProperty("lastChangeDateTime")
    private String lastChangeDateTime;

    @JsonProperty("balanceAmount")
    private BalanceAmount balanceAmount;

    public BalanceAmount getBalanceAmount() {
        return balanceAmount;
    }
}
