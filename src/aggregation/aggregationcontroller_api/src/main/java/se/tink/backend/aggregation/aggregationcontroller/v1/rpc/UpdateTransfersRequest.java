package se.tink.backend.aggregation.aggregationcontroller.v1.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import se.tink.libraries.transfer.rpc.Transfer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateTransfersRequest {

    private String credentialsId;
    private int credentialsDataVersion;
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

    public int getCredentialsDataVersion() {
        return credentialsDataVersion;
    }

    public void setCredentialsDataVersion(int credentialsDataVersion) {
        this.credentialsDataVersion = credentialsDataVersion;
    }
}
