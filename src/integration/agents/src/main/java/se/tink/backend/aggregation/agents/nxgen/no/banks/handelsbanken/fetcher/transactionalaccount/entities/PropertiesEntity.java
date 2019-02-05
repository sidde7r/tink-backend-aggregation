package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PropertiesEntity {
    private String alias;
    private String currencyCode;
    private String status;
    private String type;
    private Boolean external;
    private Integer productId;
    private String accountProduct;
    private String accountType;

    public String getAlias() {
        return alias;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getStatus() {
        return status;
    }

    public String getType() {
        return type;
    }

    public Boolean getExternal() {
        return external;
    }

    public Integer getProductId() {
        return productId;
    }

    public String getAccountProduct() {
        return accountProduct;
    }

    public String getAccountType() {
        return accountType;
    }
}
