package se.tink.backend.aggregation.agents.nxgen.se.other.csn.fetcher.loans.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class TransactionEntity {

    @JsonProperty("lopnummer")
    private int serialNumber;

    @JsonProperty("transaktionBelopp")
    private BigDecimal transactionAmount;

    @JsonProperty("laneTransaktionBeskrivning")
    private String loanTransactionDescription;

    @JsonProperty("laneTyp")
    private String loanType;

    @JsonProperty("transaktionsId")
    private double transactionId;

    @JsonIgnore
    public boolean isAmortized() {
        return loanTransactionDescription.matches("Årsbelopp \\d{4} - kapital");
    }
}
