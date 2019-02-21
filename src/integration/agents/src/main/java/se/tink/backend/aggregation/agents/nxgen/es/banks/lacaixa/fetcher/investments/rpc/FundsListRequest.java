package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FundsListRequest {
    @JsonProperty("masDatos")
    private String moreData;

    @JsonProperty("verFondosSaldoCero")
    private String showZeroBalanceFunds;

    public FundsListRequest(boolean moreData) {
        this.moreData = String.valueOf(moreData);
        this.showZeroBalanceFunds = "true";
    }
}
