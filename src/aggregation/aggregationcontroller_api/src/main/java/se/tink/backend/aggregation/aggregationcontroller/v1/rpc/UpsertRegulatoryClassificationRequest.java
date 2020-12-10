package se.tink.backend.aggregation.aggregationcontroller.v1.rpc;

import com.google.common.base.MoreObjects;
import se.tink.libraries.jersey.utils.SafelyLoggable;

public class UpsertRegulatoryClassificationRequest implements SafelyLoggable {
    private String appId;
    private String userId;
    private String accountId;
    private String credentialsId;
    private CoreRegulatoryClassification classification;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public CoreRegulatoryClassification getClassification() {
        return classification;
    }

    public void setClassification(CoreRegulatoryClassification classification) {
        this.classification = classification;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    @Override
    public String toSafeString() {
        return MoreObjects.toStringHelper(this)
                .add("appId", appId)
                .add("userId", userId)
                .add("accountId", accountId)
                .add("credentialsId", credentialsId)
                .add("classification", classification)
                .toString();
    }
}
