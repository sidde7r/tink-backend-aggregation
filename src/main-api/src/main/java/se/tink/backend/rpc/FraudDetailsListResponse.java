package se.tink.backend.rpc;

import java.util.List;

import io.protostuff.Tag;
import se.tink.backend.core.FraudDetails;

public class FraudDetailsListResponse {

    @Tag(1)
    private List<FraudDetails> fraudDetails;
    @Tag(2)
    private String fraudItemId;

    public List<FraudDetails> getFraudDetails() {
        return fraudDetails;
    }

    public void setFraudDetails(List<FraudDetails> fraudDetails) {
        this.fraudDetails = fraudDetails;
    }

    public String getFraudItemId() {
        return fraudItemId;
    }

    public void setFraudItemId(String fraudItemId) {
        this.fraudItemId = fraudItemId;
    }
}
