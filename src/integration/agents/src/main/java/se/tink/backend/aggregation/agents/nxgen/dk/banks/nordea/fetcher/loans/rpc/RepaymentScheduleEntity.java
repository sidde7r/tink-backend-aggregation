package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.loans.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class RepaymentScheduleEntity {
    private List<InstalmentFreeMonthsEntity> instalmentFreeMonths;

    private String finalPaymentDate;

    private String refinancingDate;

    private double annualAccountManagementFeePercentage;

    private int termsPerYear;
}
