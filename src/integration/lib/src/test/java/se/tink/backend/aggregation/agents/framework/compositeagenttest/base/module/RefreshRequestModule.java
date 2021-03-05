package se.tink.backend.aggregation.agents.framework.compositeagenttest.base.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import java.util.Set;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Provider;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.user.rpc.User;

public final class RefreshRequestModule extends AbstractModule {

    private final Set<RefreshableItem> refreshableItems;
    private final boolean requestFlagManual;
    private final boolean requestFlagCreate;
    private final boolean requestFlagUpdate;
    private final boolean forceAutoAuth;
    private static final String VALID_V4_UUID = "00000000-0000-4000-0000-000000000000";

    public RefreshRequestModule(
            Set<RefreshableItem> refreshableItems,
            boolean manual,
            boolean create,
            boolean update,
            boolean forceAutoAuth) {
        this.refreshableItems = refreshableItems;
        this.requestFlagManual = manual;
        this.requestFlagCreate = create;
        this.requestFlagUpdate = update;
        this.forceAutoAuth = forceAutoAuth;
    }

    public RefreshRequestModule(
            Set<RefreshableItem> refreshableItems, boolean manual, boolean create, boolean update) {
        this.refreshableItems = refreshableItems;
        this.requestFlagManual = manual;
        this.requestFlagCreate = create;
        this.requestFlagUpdate = update;
        this.forceAutoAuth = false;
    }

    @Override
    protected void configure() {
        requireBinding(User.class);
        requireBinding(Credentials.class);
        requireBinding(Provider.class);

        bind(CredentialsRequest.class).to(RefreshInformationRequest.class).in(Scopes.SINGLETON);
    }

    @Provides
    @Singleton
    protected RefreshInformationRequest provideRefreshInformationRequest(
            User user, Credentials credential, Provider provider, String originatingUserIp) {

        // Hack required due to logic in AutoAuthenticationController#shouldDoManualAuthentication
        // During auto refresh all agents have CredentialsTypes.PASSWORD
        boolean forceFullAuth = true;
        if (forceAutoAuth) {
            credential.setType(CredentialsTypes.PASSWORD);
            forceFullAuth = false;
        }

        RefreshInformationRequest refreshInformationRequest =
                RefreshInformationRequest.builder()
                        .user(user)
                        .provider(provider)
                        .credentials(credential)
                        .originatingUserIp(originatingUserIp)
                        .manual(requestFlagManual)
                        .create(requestFlagCreate)
                        .update(requestFlagUpdate)
                        .forceAuthenticate(forceFullAuth)
                        .build();
        refreshInformationRequest.setItemsToRefresh(refreshableItems);
        refreshInformationRequest.setAppUriId(VALID_V4_UUID);
        return refreshInformationRequest;
    }
}
