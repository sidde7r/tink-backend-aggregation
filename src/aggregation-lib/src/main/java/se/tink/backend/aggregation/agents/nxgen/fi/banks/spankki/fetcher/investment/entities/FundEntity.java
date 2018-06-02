package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FundEntity {
    private String id;
    private String isin;
    private String categoryId;
    private String categoryName;
    private String fullName;
    private String brandGroupName;
    private String name;

    public String getId() {
        return id;
    }

    public String getIsin() {
        return isin;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getFullName() {
        return fullName;
    }

    public String getBrandGroupName() {
        return brandGroupName;
    }

    public String getName() {
        return name;
    }
}
