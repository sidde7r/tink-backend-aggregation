package se.tink.backend.nasa.boot.rpc;

import java.util.Set;

public class RefreshInformationRequest extends CredentialsRequest {

    private boolean manual;
    private Set<RefreshableItem> itemsToRefresh;

    public RefreshInformationRequest() {}

    public RefreshInformationRequest(
            User user, Provider provider, Credentials credentials, boolean manual) {
        super(credentials, provider, user);
        this.manual = manual;
    }

    @Override
    public boolean isManual() {
        return false;
    }

    @Override
    public CredentialsRequestType getType() {
        return CredentialsRequestType.REFRESH_INFORMATION;
    }

    public void setManual(boolean manual) {
        this.manual = manual;
    }

    public void setItemsToRefresh(Set<RefreshableItem> itemsToRefresh) {
        this.itemsToRefresh = itemsToRefresh;
    }
}
