package se.tink.backend.aggregation.aggregationcontroller.v1.rpc;

import com.google.common.base.MoreObjects;

public class SupplementalInformationRequest {
    private String credentialsId;

    public String getCredentialsId() {
        return credentialsId;
    }

    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("credentialsId", credentialsId).toString();
    }
}
