package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.account.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ItemsEntity {
    private String displayCustomerNumber;
    private String customerNumber;
    private String customerName;
    private String accountOwnerTitle;
    private boolean primary;
    private List<ProductsEntity> products;

    public String getDisplayCustomerNumber() {
        return displayCustomerNumber;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getAccountOwnerTitle() {
        return accountOwnerTitle;
    }

    public boolean isPrimary() {
        return primary;
    }

    public List<ProductsEntity> getProducts() {
        return products;
    }
}
