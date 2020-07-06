package se.tink.backend.aggregation.aggregationcontroller.v1.rpc;

public class UpsertRegulatoryClassificationRequest {
    private String appId;
    private String userId;
    private String accountId;
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
}
