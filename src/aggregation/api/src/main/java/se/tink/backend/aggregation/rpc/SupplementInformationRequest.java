package se.tink.backend.aggregation.rpc;

public class SupplementInformationRequest {
    public static final String QUEUE_NAME = "supplementInformation";

    private String credentialsId;
    private String supplementalInformation;

    public String getCredentialsId() {
        return credentialsId;
    }

    public String getSupplementalInformation() {
        return supplementalInformation;
    }

    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    public void setSupplementalInformation(String supplementalInformation) {
        this.supplementalInformation = supplementalInformation;
    }
}
