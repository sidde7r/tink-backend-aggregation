package se.tink.backend.system.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.models.fraud.FraudDetailsContent;

public class UpdateFraudDetailsRequest {
    private List<FraudDetailsContent> detailsContents;
    private String userId;

    public List<FraudDetailsContent> getDetailsContents() {
        return detailsContents;
    }

    public void setDetailsContents(List<FraudDetailsContent> detailsContents) {
        this.detailsContents = detailsContents;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
