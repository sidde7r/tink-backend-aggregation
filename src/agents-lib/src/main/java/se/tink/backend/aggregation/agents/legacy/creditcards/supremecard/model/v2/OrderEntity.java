package se.tink.backend.aggregation.agents.creditcards.supremecard.model.v2;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderEntity {
    @JsonProperty("autoStartToken")
    private String autoStartToken;
    @JsonProperty("collectUrl")
    private String collectUrl;
    @JsonProperty("orderRef")
    private String orderRef;

    public String getAutoStartToken() {
        return autoStartToken;
    }

    public void setAutoStartToken(String autoStartToken) {
        this.autoStartToken = autoStartToken;
    }

    public String getCollectUrl() {
        return collectUrl;
    }

    public void setCollectUrl(String collectUrl) {
        this.collectUrl = collectUrl;
    }

    public String getOrderRef() {
        return orderRef;
    }

    public void setOrderRef(String orderRef) {
        this.orderRef = orderRef;
    }
}
