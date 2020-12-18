package se.tink.backend.aggregation.agents.nxgen.se.other.csn.fetcher.loans.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public class LoanEntity {

    @JsonProperty("skuldspecifikation")
    private List<DebtDetailEntity> debtSpecification;

    @JsonProperty("laneTypKlartext")
    private String loanTypePlainText;

    @JsonProperty("lopnummer")
    private int serialNumber;

    @JsonProperty("senasteBerakningsdatum")
    private long latestCalculationDate;

    @JsonProperty("lanetyp")
    private String loanType;

    @JsonProperty("skuldbelopp")
    private int debtAmount;

    @JsonProperty("skuldrattat")
    private String debtCorrected;

    @JsonProperty("klartext")
    private boolean isPlainText;

    public BigDecimal getIncomingDebt() {
        return debtSpecification.stream()
                .filter(DebtDetailEntity::isIncomingDebt)
                .map(DebtDetailEntity::getAmount)
                .findFirst()
                .orElse(null);
    }

    public BigDecimal getOutgoingDebt() {
        return debtSpecification.stream()
                .filter(DebtDetailEntity::isOutgoingDebt)
                .map(DebtDetailEntity::getAmount)
                .findFirst()
                .orElse(null);
    }

    public BigDecimal getInterest() {
        return debtSpecification.stream()
                .filter(DebtDetailEntity::isInterest)
                .map(DebtDetailEntity::getAmount)
                .findFirst()
                .orElse(null);
    }
}
