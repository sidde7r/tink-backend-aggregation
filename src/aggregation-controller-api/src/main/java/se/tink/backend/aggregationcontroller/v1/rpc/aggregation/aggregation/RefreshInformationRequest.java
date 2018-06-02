package se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;
import se.tink.backend.aggregationcontroller.v1.rpc.entities.Credentials;
import se.tink.backend.aggregationcontroller.v1.rpc.entities.Provider;
import se.tink.backend.aggregationcontroller.v1.rpc.entities.User;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.RefreshableItem;

public class RefreshInformationRequest extends CredentialsRequest {
    @JsonProperty
    private boolean manual;
    private Set<RefreshableItem> itemsToRefresh;

    public RefreshInformationRequest() {

    }

    public RefreshInformationRequest(User user, Provider provider, Credentials credentials, boolean manual, boolean create, boolean update) {
        super(user, provider, credentials);

        this.manual = manual;
        this.create = create;
        this.update = update;
    }

    public RefreshInformationRequest(User user, Provider provider, Credentials credentials, boolean manual) {
        this(user, provider, credentials, manual, false, false);
    }

    @Override
    public CredentialsRequestType getType() {
        return CredentialsRequestType.REFRESH_INFORMATION;
    }

    public void setManual(boolean manual) {
        this.manual = manual;
    }

    @Override
    public boolean isManual() {
        return manual;
    }

    public Set<RefreshableItem> getItemsToRefresh() {
        return itemsToRefresh;
    }

    public void setItemsToRefresh(Set<RefreshableItem> itemsToRefresh) {
        this.itemsToRefresh = itemsToRefresh;
    }
}
