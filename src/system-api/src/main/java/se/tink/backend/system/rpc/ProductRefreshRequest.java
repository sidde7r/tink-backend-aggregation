package se.tink.backend.system.rpc;

import com.google.common.base.Preconditions;

public class ProductRefreshRequest {
    private String userId;

    public ProductRefreshRequest() {

    }

    public ProductRefreshRequest(String userId) {
        Preconditions.checkNotNull(userId);

        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        Preconditions.checkNotNull(userId);

        this.userId = userId;
    }
}
