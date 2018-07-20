package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CategoryEntity {
    private int id;
    private int categoryContextId;
    private int orderId;
    private String name;
    private String otherCategoryName;

    public int getId() {
        return id;
    }

    public int getCategoryContextId() {
        return categoryContextId;
    }

    public int getOrderId() {
        return orderId;
    }

    public String getName() {
        return name;
    }

    public String getOtherCategoryName() {
        return otherCategoryName;
    }
}

