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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Credentials credentials;
        private User user;
        private Provider provider;
        private String originatingUserIp;
        private boolean manual;
        private boolean create = false;
        private boolean update = false;
        private boolean forceAuthenticate;

        public Builder credentials(Credentials credentials) {
            this.credentials = credentials;
            return this;
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder provider(Provider provider) {
            this.provider = provider;
            return this;
        }

        public Builder originatingUserIp(String originatingUserIp) {
            this.originatingUserIp = originatingUserIp;
            return this;
        }

        public Builder manual(boolean manual) {
            this.manual = manual;
            return this;
        }

        public Builder create(boolean create) {
            this.create = create;
            return this;
        }

        public Builder update(boolean update) {
            this.update = update;
            return this;
        }

        public Builder forceAuthenticate(boolean forceAuthenticate) {
            this.forceAuthenticate = forceAuthenticate;
            return this;
        }

        public RefreshInformationRequest build() {
            RefreshInformationRequest refreshInformationRequest = new RefreshInformationRequest();

            refreshInformationRequest.setCredentials(credentials);
            refreshInformationRequest.setUser(user);
            refreshInformationRequest.setProvider(provider);
            refreshInformationRequest.setOriginatingUserIp(originatingUserIp);
            refreshInformationRequest.setManual(manual);
            refreshInformationRequest.setCreate(create);
            refreshInformationRequest.setUpdate(update);
            refreshInformationRequest.setForceAuthenticate(forceAuthenticate);
            return refreshInformationRequest;
        }
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
