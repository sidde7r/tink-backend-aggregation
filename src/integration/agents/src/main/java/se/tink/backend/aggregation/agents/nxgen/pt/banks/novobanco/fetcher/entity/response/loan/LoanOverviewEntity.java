package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.loan;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanOverviewEntity {
    @JsonProperty("Produto")
    private String product;

    @JsonProperty("Contrato")
    private String contract;

    @JsonProperty("Saldo")
    private double balance;

    public String getProduct() {
        return product;
    }

    public String getContract() {
        return contract;
    }

    public double getBalance() {
        return balance;
    }
}
