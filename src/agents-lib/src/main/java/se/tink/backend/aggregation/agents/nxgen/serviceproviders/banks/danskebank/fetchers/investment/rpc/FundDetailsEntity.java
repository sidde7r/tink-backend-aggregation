package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FundDetailsEntity {
    private double yearlyCost;
    private double totalExpenseRatio;
    private double yearlyAgencyCommision;
    private int morningstarRating;
    private String morningstarRatingDate;
    private double standardDeviation;
    private double sharpRatio;

    public double getYearlyCost() {
        return yearlyCost;
    }

    public double getTotalExpenseRatio() {
        return totalExpenseRatio;
    }

    public double getYearlyAgencyCommision() {
        return yearlyAgencyCommision;
    }

    public int getMorningstarRating() {
        return morningstarRating;
    }

    public String getMorningstarRatingDate() {
        return morningstarRatingDate;
    }

    public double getStandardDeviation() {
        return standardDeviation;
    }

    public double getSharpRatio() {
        return sharpRatio;
    }
}
