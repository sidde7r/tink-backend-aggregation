package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.loan.rpc;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.loan.entities.PaymentDetailEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class LoanDetailsResponse {
    private BigDecimal loanAmount;
    private long originalLoanAmount;
    private String loanCategory;
    private double nominalInterestRate;
    private double effectiveInterestRate;
    private long loanTermYears;
    private long closureDate;
    private String id;
    private List<PaymentDetailEntity> paymentDetail;
}
