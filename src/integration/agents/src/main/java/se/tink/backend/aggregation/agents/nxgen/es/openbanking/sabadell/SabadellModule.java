package se.tink.backend.aggregation.agents.nxgen.es.openbanking.sabadell;

import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import se.tink.backend.aggregation.agents.consent.ConsentGenerator;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.redsys.RedsysDetailedConsentGenerator;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.redsys.RedsysScope;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.redsys.rpc.ConsentRequestBody;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public class SabadellModule extends AbstractModule {

    @Provides
    @Singleton
    @Inject
    public ConsentGenerator<ConsentRequestBody> createConsentGenerator(
            AgentComponentProvider agentComponentProvider) {
        return RedsysDetailedConsentGenerator.builder()
                .componentProvider(agentComponentProvider)
                .availableScopes(
                        Sets.newHashSet(
                                RedsysScope.ACCOUNTS,
                                RedsysScope.BALANCES,
                                RedsysScope.TRANSACTIONS))
                .forUserSpecifiedAccounts()
                .build();
    }
}
