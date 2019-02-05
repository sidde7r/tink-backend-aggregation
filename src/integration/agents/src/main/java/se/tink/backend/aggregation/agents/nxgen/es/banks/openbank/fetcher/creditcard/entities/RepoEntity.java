package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RepoEntity {
    @JsonProperty("indPosCursorRepo")
    private String indPosCursorRepo;

    @JsonProperty("fechaOperacionRepo")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date operationDate;

    @JsonProperty("diaRepo")
    private int diaRepo;

    @JsonProperty("fechaAnotacionMovRepo")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date transactionDate;

    @JsonProperty("tipoSaldoRepo")
    private String balanceType;

    @JsonProperty("divisaRepo")
    private String currency;

    public String getIndPosCursorRepo() {
        return indPosCursorRepo;
    }

    public Date getOperationDate() {
        return operationDate;
    }

    public int getDiaRepo() {
        return diaRepo;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public String getBalanceType() {
        return balanceType;
    }

    public String getCurrency() {
        return currency;
    }
}
