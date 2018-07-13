package se.tink.backend.aggregation.agents.creditcards.supremecard.model.v2;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CollectRequest {
    @JsonProperty("orderRef")
    private String orderRef;

    public String getOrderRef() {
        return orderRef;
    }

    public void setOrderRef(String orderRef) {
        this.orderRef = orderRef;
    }
}
