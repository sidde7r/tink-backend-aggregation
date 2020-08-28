package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SoleTraderAccountHolderResponse {

    private String tradingAsName;
    private String businessCategory;
    private String businessSubCategory;

    public String getTradingAsName() {
        return tradingAsName;
    }

    public String getBusinessCategory() {
        return businessCategory;
    }

    public String getBusinessSubCategory() {
        return businessSubCategory;
    }
}
