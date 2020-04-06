package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FundDetailsEntity {
    private int fundId;
    private String name;
    private String company;
    private int risk;
    private String riskText;
    private String type;
    private HoldingEntity holding;

    public int getFundId() {
        return fundId;
    }

    public String getName() {
        return name;
    }

    public String getCompany() {
        return company;
    }

    public int getRisk() {
        return risk;
    }

    public String getRiskText() {
        return riskText;
    }

    public String getType() {
        return type;
    }

    public HoldingEntity getHolding() {
        return holding;
    }
}
