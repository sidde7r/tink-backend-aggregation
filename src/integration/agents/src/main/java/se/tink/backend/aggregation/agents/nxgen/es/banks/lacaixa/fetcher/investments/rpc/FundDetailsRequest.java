package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FundDetailsRequest {
    @JsonProperty("refValExpediente")
    private final String fundReference;
    @JsonProperty("codigoFondo")
    private final String fundCode;
    @JsonProperty("divisa")
    private final String currency;
    private final String alias = "";

    public FundDetailsRequest(String fundReference, String fundCode, String currency) {
        this.fundReference = fundReference;
        this.fundCode = fundCode;
        this.currency = currency;
    }
}
