package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FundInfoEntity {

    private String isIn;
    private String name;
    private String fundTypeName;
    private String fundCode;

    public String getIsIn() {
        return isIn;
    }

    public String getName() {
        return name;
    }

    public String getFundTypeName() {
        return fundTypeName;
    }

    public String getFundCode() {
        return fundCode;
    }
}
