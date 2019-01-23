package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanDetailsEntity {
    private double grantedAmount;
    @JsonFormat(pattern = "yyyyMMdd")
    private Date openingdate;
    @JsonFormat(pattern = "yyyyMMdd")
    private Date nextInterestAdjustmentDate;

    public double getGrantedAmount() {
        return grantedAmount;
    }

    public Date getOpeningDate() {
        return openingdate;
    }

    public Date getNextInterestAdjustmentDate() {
        return nextInterestAdjustmentDate;
    }
}
