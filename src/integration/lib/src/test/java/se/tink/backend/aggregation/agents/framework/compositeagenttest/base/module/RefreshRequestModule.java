package se.tink.backend.aggregation.agents.framework.compositeagenttest.base.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import java.util.Set;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.user.rpc.User;

public final class RefreshRequestModule extends AbstractModule {

    private final Set<RefreshableItem> refreshableItems;

    public RefreshRequestModule(Set<RefreshableItem> refreshableItems) {
        this.refreshableItems = refreshableItems;
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
            User user, Credentials credential, Provider provider) {
        RefreshInformationRequest refreshInformationRequest =
                new RefreshInformationRequest(user, provider, credential, true);
        refreshInformationRequest.setItemsToRefresh(refreshableItems);
        return refreshInformationRequest;
    }
}
