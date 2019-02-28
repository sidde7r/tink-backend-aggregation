package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.creditcards.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProductEntity {

    private String productKey;
    private String name;
    private boolean isVisa;
    private boolean isMasterCard;
    private String businessCard;
    private String cobranded;

    public String getProductKey() {
        return productKey;
    }

    public String getName() {
        return name;
    }

    public boolean isVisa() {
        return isVisa;
    }

    public boolean isMasterCard() {
        return isMasterCard;
    }

    public String getBusinessCard() {
        return businessCard;
    }

    public String getCobranded() {
        return cobranded;
    }
}
