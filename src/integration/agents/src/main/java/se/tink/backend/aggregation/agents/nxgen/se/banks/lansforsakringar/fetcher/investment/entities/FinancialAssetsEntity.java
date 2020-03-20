package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FinancialAssetsEntity {
    private String managementForm;
    private double numberOfShares;
    private boolean isTradingAllowed;
    private double value;
    private int valuePercent;
    private double rate;
    private String rateDate;
    private double performancePercent;
    private String performanceSince;
    private FundEntity fund;
}
