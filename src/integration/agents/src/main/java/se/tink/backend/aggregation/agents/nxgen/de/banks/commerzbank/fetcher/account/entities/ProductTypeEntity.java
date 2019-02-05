package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.account.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProductTypeEntity {
    private String category;
    private String displayCategory;
    private int displayCategoryIndex;
    private String ccbId;
    private String productName;
    private String productBranch;

    public String getCategory() {
        return category;
    }

    public String getDisplayCategory() {
        return displayCategory;
    }

    public int getDisplayCategoryIndex() {
        return displayCategoryIndex;
    }

    public String getCcbId() {
        return ccbId;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductBranch() {
        return productBranch;
    }
}
