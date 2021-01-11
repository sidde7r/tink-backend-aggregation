package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.tesco;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.configuration.UkOpenBankingConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.signer.EidasJwsSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.signer.EidasProxyJwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.provider.UkOpenBankingConfigurationProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public class TescoModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(UkOpenBankingConfiguration.class)
                .toProvider(UkOpenBankingConfigurationProvider.class)
                .in(Scopes.SINGLETON);
    }

    // Tesco has been registered using Qwac/Qseal and it's still working (after 01.01.2021, where
    // the support should get expired). This is the workaround till we're gonna be able to register
    // the application using Obwac/Obseal
    @Provides
    @Singleton
    @Inject
    public JwtSigner jwtSigner(
            UkOpenBankingConfiguration openBankingConfiguration,
            AgentComponentProvider agentComponentProvider,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        CompositeAgentContext context = agentComponentProvider.getContext();
        EidasIdentity eidasIdentity =
                new EidasIdentity(
                        context.getClusterId(), context.getAppId(), "DEFAULT", TescoV31Agent.class);
        EidasJwsSigner eidasJwsSigner =
                new EidasJwsSigner(
                        agentsServiceConfiguration.getEidasProxy().toInternalConfig(),
                        eidasIdentity);
        return new EidasProxyJwtSigner(openBankingConfiguration::getSigningKeyId, eidasJwsSigner);
    }
}
