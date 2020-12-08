package se.tink.backend.aggregation.aggregationcontroller.v1.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;
import java.util.List;
import se.tink.libraries.jersey.utils.SafelyLoggable;
import se.tink.libraries.transfer.rpc.Transfer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateTransfersRequest implements SafelyLoggable {

    private String credentialsId;
    private List<Transfer> transfers;
    private String userId;

    public String getCredentialsId() {
        return credentialsId;
    }

    public List<Transfer> getTransfers() {
        return transfers;
    }

    public String getUserId() {
        return userId;
    }

    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    public void setTransfers(List<Transfer> transfers) {
        this.transfers = transfers;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toSafeString() {
        return MoreObjects.toStringHelper(this)
                .add("credentialsId", credentialsId)
                .add("transfers", transfers)
                .add("userId", userId)
                .toString();
    }
}
