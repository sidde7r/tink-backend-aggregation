package se.tink.backend.system.rpc;

import com.google.common.base.MoreObjects;
import java.util.List;
import se.tink.backend.aggregation.agents.models.fraud.FraudDetailsContent;
import se.tink.libraries.jersey.utils.SafelyLoggable;

public class UpdateFraudDetailsRequest implements SafelyLoggable {
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

    @Override
    public String toSafeString() {
        return MoreObjects.toStringHelper(this)
                .add("detailsContents", detailsContents)
                .add("userId", userId)
                .toString();
    }
}
