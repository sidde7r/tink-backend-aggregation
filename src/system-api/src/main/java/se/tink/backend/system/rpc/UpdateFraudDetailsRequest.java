package se.tink.backend.system.rpc;

import se.tink.backend.core.FraudDetailsContent;

import java.util.List;

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
