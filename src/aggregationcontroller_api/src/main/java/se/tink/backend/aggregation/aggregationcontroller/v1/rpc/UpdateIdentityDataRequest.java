package se.tink.backend.aggregation.aggregationcontroller.v1.rpc;

public class UpdateIdentityDataRequest {
    private IdentityData identityData;
    private String userId;
    private String providerName;

    public IdentityData getIdentityData() {
        return identityData;
    }

    public void setIdentityData(IdentityData identityData) {
        this.identityData = identityData;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }
}
