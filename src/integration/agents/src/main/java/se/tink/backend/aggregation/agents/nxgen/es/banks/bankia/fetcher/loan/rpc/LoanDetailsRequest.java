package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanDetailsRequest {
    @JsonProperty("clavePaginacionEntrada")
    private String continuationKey = "";

    @JsonProperty("codigoLineaRiesgo")
    private String code;

    @JsonProperty("identificadorExpediente")
    private String loanIdentifier;

    public LoanDetailsRequest(String code) {
        this.code = code;
    }

    public LoanDetailsRequest setLoanIdentifier(String loanIdentifier) {
        this.loanIdentifier = loanIdentifier;

        return this;
    }

    public LoanDetailsRequest setContinuationKey(String continuationKey) {
        this.continuationKey = continuationKey;

        return this;
    }
}
