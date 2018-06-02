package se.tink.backend.rpc;

import java.util.List;

import se.tink.backend.core.FraudItem;

public class FraudItemsResponse {

    private List<FraudItem> fraudItems;

    public List<FraudItem> getFraudItems() {
        return fraudItems;
    }
    public void setFraudItems(List<FraudItem> fraudItems) {
        this.fraudItems = fraudItems;
    }
}
