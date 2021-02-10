package se.tink.backend.aggregation.agents.nxgen.se.other.csn.fetcher.loans.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class LoanTransactionsResponse {

    @JsonProperty("inbetalningar")
    private List<PaymentEntity> payments;

    @JsonProperty("arsbearbetningPagar")
    private boolean isAnnualProcessing;

    @JsonProperty("uppsagtLanFinns")
    private boolean isLoanTerminated;

    @JsonProperty("skuldsanering")
    private boolean debtCorrection;

    @JsonIgnore
    public BigDecimal getTotalAmortization() {
        return payments.stream()
                .map(PaymentEntity::getAmortization)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
