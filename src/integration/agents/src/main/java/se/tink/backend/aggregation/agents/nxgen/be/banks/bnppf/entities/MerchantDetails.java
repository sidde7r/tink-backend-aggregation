package se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MerchantDetails {
    private String merchantName;
    private String merchantCategoryCode;
    private String merchantAccountId;

    public String getMerchantName() {
        return merchantName;
    }

    public String getMerchantCategoryCode() {
        return merchantCategoryCode;
    }

    public String getMerchantAccountId() {
        return merchantAccountId;
    }
}
