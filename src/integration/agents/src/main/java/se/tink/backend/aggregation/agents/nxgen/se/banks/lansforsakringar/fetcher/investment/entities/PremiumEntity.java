package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PremiumEntity {
    private double agreedPremium;
    private String premiumPaymentMethod;
    private double totalPremium;
    private String paidUntilDate;
    private double paidYearToDate;
}
