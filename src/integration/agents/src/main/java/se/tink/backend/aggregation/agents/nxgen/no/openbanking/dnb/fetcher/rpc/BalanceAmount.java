package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceAmount {

    private String amount;
    private String currency;

    public String getAmount() {
        return amount;
    }
}
