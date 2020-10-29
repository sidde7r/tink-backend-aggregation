package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.rpc;

import java.math.BigDecimal;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class FundDetailsEntity {
    private BigDecimal yearlyCost;
    private BigDecimal totalExpenseRatio;
    private BigDecimal yearlyAgencyCommision;
    private int morningstarRating;
    private String morningstarRatingDate;
    private BigDecimal standardDeviation;
    private BigDecimal sharpRatio;
}
