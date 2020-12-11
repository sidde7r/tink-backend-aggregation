package se.tink.backend.aggregation.aggregationcontroller.v1.rpc;

import com.google.common.base.MoreObjects;
import se.tink.libraries.jersey.utils.SafelyLoggable;

public class UpdateIdentityDataRequest implements SafelyLoggable {
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

    @Override
    public String toSafeString() {
        return MoreObjects.toStringHelper(this)
                .add("userId", userId)
                .add("providerName", providerName)
                .add("identityData", identityData == null ? null : "***")
                .toString();
    }
}
