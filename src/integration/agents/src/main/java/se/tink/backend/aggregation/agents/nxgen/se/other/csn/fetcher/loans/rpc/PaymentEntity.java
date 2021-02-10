package se.tink.backend.aggregation.agents.nxgen.se.other.csn.fetcher.loans.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class PaymentEntity {

    @JsonProperty("bokforingsdatum")
    private long accountingDate;

    @JsonProperty("totalbelopp")
    private BigDecimal totalAmount;

    @JsonProperty("transaktioner")
    private List<TransactionEntity> transactions;

    @JsonProperty("lanetyp")
    private String loanType;

    @JsonProperty("transaktionsId")
    private double transactionsId;

    @JsonIgnore
    public BigDecimal getAmortization() {
        return transactions.stream()
                .filter(TransactionEntity::isAmortized)
                .map(TransactionEntity::getTransactionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
