package se.tink.backend.aggregationcontroller.v1.rpc.system.update;

public class SupplementalInformationRequest {
    private String credentialsId;

    public String getCredentialsId() {
        return credentialsId;
    }

    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }
}
