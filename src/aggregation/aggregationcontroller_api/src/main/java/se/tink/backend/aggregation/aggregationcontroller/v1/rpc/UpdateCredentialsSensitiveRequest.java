package se.tink.backend.aggregation.aggregationcontroller.v1.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateCredentialsSensitiveRequest {
    private String userId;
    private String credentialsId;
    private int credentialsDataVersion;
    private String sensitiveData;
    private String operationId;

    public String getUserId() {
        return userId;
    }

    public UpdateCredentialsSensitiveRequest setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public UpdateCredentialsSensitiveRequest setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
        return this;
    }

    public String getSensitiveData() {
        return sensitiveData;
    }

    public UpdateCredentialsSensitiveRequest setSensitiveData(String sensitiveData) {
        this.sensitiveData = sensitiveData;
        return this;
    }

    public int getCredentialsDataVersion() {
        return credentialsDataVersion;
    }

    public UpdateCredentialsSensitiveRequest setCredentialsDataVersion(int credentialsDataVersion) {
        this.credentialsDataVersion = credentialsDataVersion;
        return this;
    }

    public String getOperationId() {
        return operationId;
    }

    public UpdateCredentialsSensitiveRequest setOperationId(String operationId) {
        this.operationId = operationId;
        return this;
    }
}
