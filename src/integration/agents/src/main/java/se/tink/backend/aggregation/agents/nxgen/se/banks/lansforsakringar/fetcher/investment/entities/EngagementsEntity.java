package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EngagementsEntity {
    private String name;
    private String productName;
    private String id;
    private String type;
    //    @JsonIgnore private List<RiskCoversEntity> riskCovers;
    private String status;
    private double investmentValue;
    // `agreedPremium` is null - cannot define it!
    // `premiumModelTerm` is null - cannot define it!
    private String owner;
    private String ownerId;
    private String productType;
    private String productCategory;
    private boolean hasDetail;

    public String getName() {
        return name;
    }

    public String getProductName() {
        return productName;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public double getInvestmentValue() {
        return investmentValue;
    }

    public String getOwner() {
        return owner;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getProductType() {
        return productType;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public boolean isHasDetail() {
        return hasDetail;
    }
}
