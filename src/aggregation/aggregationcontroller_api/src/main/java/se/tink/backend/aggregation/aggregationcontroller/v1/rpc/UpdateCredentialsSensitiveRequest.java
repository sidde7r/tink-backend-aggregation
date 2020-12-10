package se.tink.backend.aggregation.aggregationcontroller.v1.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;
import se.tink.libraries.jersey.utils.SafelyLoggable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateCredentialsSensitiveRequest implements SafelyLoggable {
    private String userId;
    private String credentialsId;
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

    public String getOperationId() {
        return operationId;
    }

    public UpdateCredentialsSensitiveRequest setOperationId(String operationId) {
        this.operationId = operationId;
        return this;
    }

    @JsonIgnore
    @Override
    public String toSafeString() {
        return MoreObjects.toStringHelper(this)
                .add("userId", userId)
                .add("credentialsId", credentialsId)
                .add("sensitiveData", sensitiveData)
                .add("operationId", operationId)
                .toString();
    }
}
