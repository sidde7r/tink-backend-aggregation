package se.tink.libraries.credentials.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.libraries.user.rpc.User;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RefreshInformationRequest extends CredentialsRequest {
    @JsonProperty private boolean manual;
    private Set<RefreshableItem> itemsToRefresh;
    private String refreshId;

    @JsonProperty("forceAuthenticate")
    private boolean forceAuthenticate;

    public RefreshInformationRequest() {}

    public RefreshInformationRequest(
            User user,
            Provider provider,
            Credentials credentials,
            boolean manual,
            boolean create,
            boolean update,
            boolean forceAuthenticate) {
        super(user, provider, credentials);

        this.manual = manual;
        this.create = create;
        this.update = update;
        this.forceAuthenticate = forceAuthenticate;
    }

    public RefreshInformationRequest(
            User user,
            Provider provider,
            Credentials credentials,
            boolean manual,
            boolean forceAuthenticate) {
        this(user, provider, credentials, manual, false, false, forceAuthenticate);
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

    public String getRefreshId() {
        return refreshId;
    }

    public void setRefreshId(String refreshId) {
        this.refreshId = refreshId;
    }

    public boolean isForceAuthenticate() {
        return forceAuthenticate;
    }

    public void setForceAuthenticate(boolean forceAuthenticate) {
        this.forceAuthenticate = forceAuthenticate;
    }
}
