package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.signer;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.configuration.DanskebankEUConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.configuration.DanskebankEUConfigurationProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingFlowFacade;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.configuration.UkOpenBankingClientConfigurationAdapter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.tls.EidasProxyTlsConfigurationSetter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.tls.TlsConfigurationSetter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;

public class DanskeOpenBankingModule extends AbstractModule {
    private static final Class<DanskebankEUConfiguration> CONFIGURATION_CLASS =
            DanskebankEUConfiguration.class;

    @Override
    protected void configure() {
        bind(DanskebankEUConfiguration.class)
                .toProvider(DanskebankEUConfigurationProvider.class)
                .in(Scopes.SINGLETON);
        bind(JwtSigner.class).toProvider(DanskeJwtSignerProvider.class).in(Scopes.SINGLETON);
    }

    @Inject
    @Singleton
    @Provides
    public UkOpenBankingFlowFacade danskeFlowFacade(
            EidasIdentity eidasIdentity,
            CompositeAgentContext context,
            AgentsServiceConfiguration agentsServiceConfiguration,
            JwtSigner jwtSigner) {
        AgentConfiguration<? extends UkOpenBankingClientConfigurationAdapter> configuration =
                getAgentConfiguration(context);

        return new UkOpenBankingFlowFacade(
                createEidasProxyTlsConfigurationSetter(agentsServiceConfiguration),
                jwtSigner,
                configuration,
                eidasIdentity);
    }

    @Inject
    @Singleton
    @Provides
    public EidasIdentity eidasIdentity(CompositeAgentContext context) {
        return new EidasIdentity(
                context.getClusterId(), context.getAppId(), DanskeJwtSignerProvider.class);
    }

    private TlsConfigurationSetter createEidasProxyTlsConfigurationSetter(
            AgentsServiceConfiguration configuration) {
        return new EidasProxyTlsConfigurationSetter(configuration);
    }

    private AgentConfiguration<? extends UkOpenBankingClientConfigurationAdapter>
            getAgentConfiguration(CompositeAgentContext context) {
        return context.getAgentConfigurationController().getAgentConfiguration(CONFIGURATION_CLASS);
    }
}
