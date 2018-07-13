package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.investment.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FundRatingEntity {
    private String information;
    private int risk;
    private int morningstar;
    private int performance;
    private String detailedInformation;

    public String getInformation() {
        return information;
    }

    public int getRisk() {
        return risk;
    }

    public int getMorningstar() {
        return morningstar;
    }

    public int getPerformance() {
        return performance;
    }

    public String getDetailedInformation() {
        return detailedInformation;
    }
}
