package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.agentplatform.AgentPlatformHttpClient;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.MetroAuthenticationModule;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.MetroFetchersModule;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.session.MetroSessionHandlerModule;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.handler.AgentPlatformHttpResponseStatusHandler;

public class MetroModule extends AbstractModule {

    private static final Map<String, AccountType> ACCOUNT_TYPE_AND_PROVIDER_NAME = new HashMap<>();
    private static final String ERROR_MESSAGE_TEMPLATE =
            "Could not find proper AccountType for `%s` provider. Available providers: `%s`";

    public MetroModule() {
        ACCOUNT_TYPE_AND_PROVIDER_NAME.put("uk-metro-personal-password", AccountType.PERSONAL);
        ACCOUNT_TYPE_AND_PROVIDER_NAME.put("uk-metro-business-password", AccountType.BUSINESS);
    }

    @Override
    protected void configure() {
        install(new MetroAuthenticationModule());
        install(new MetroFetchersModule());
        install(new MetroSessionHandlerModule());
    }

    @Inject
    @Singleton
    @Provides
    public AgentPlatformHttpClient agentPlatformHttpClient(
            AgentComponentProvider componentProvider) {
        TinkHttpClient tinkHttpClient = componentProvider.getTinkHttpClient();
        tinkHttpClient.setResponseStatusHandler(new AgentPlatformHttpResponseStatusHandler());
        tinkHttpClient.disableSignatureRequestHeader();
        return new AgentPlatformHttpClient(tinkHttpClient);
    }

    @Inject
    @Singleton
    @Provides
    public AccountType getAgentType(AgentComponentProvider componentProvider) {
        String providerName = componentProvider.getCredentialsRequest().getProvider().getName();
        return Optional.ofNullable(ACCOUNT_TYPE_AND_PROVIDER_NAME.get(providerName))
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        String.format(
                                                ERROR_MESSAGE_TEMPLATE,
                                                providerName,
                                                ACCOUNT_TYPE_AND_PROVIDER_NAME.toString())));
    }
}
