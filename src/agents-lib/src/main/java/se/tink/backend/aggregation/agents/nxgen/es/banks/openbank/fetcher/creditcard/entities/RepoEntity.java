package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RepoEntity {
    @JsonProperty("indPosCursorRepo")
    private String indPosCursorRepo;

    @JsonProperty("fechaOperacionRepo")
    private String operationDate;

    @JsonProperty("diaRepo")
    private int diaRepo;

    @JsonProperty("fechaAnotacionMovRepo")
    private String transactionDate;

    @JsonProperty("tipoSaldoRepo")
    private String balanceType;

    @JsonProperty("divisaRepo")
    private String currency;

    public String getIndPosCursorRepo() {
        return indPosCursorRepo;
    }

    public String getOperationDate() {
        return operationDate;
    }

    public int getDiaRepo() {
        return diaRepo;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public String getBalanceType() {
        return balanceType;
    }

    public String getCurrency() {
        return currency;
    }
}
