package se.tink.backend.aggregation.agents.nxgen.ie.openbanking.aib;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.module.agentclass.AgentClass;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingFlowFacade;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.configuration.UkOpenBankingClientConfigurationAdapter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.tls.EidasProxyTlsConfigurationSetter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.tls.TlsConfigurationSetter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.agents.tools.jwt.signer.EidasProxyJwtSignerProvider;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidasidentity.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public class AibModule extends AbstractModule {

    private static final Class<AibClientConfiguration> CONFIGURATION_CLASS =
            AibClientConfiguration.class;

    @Inject
    @Singleton
    @Provides
    public UkOpenBankingFlowFacade aibFlowFacade(
            CompositeAgentContext context,
            AgentComponentProvider agentComponentProvider,
            AgentsServiceConfiguration agentsServiceConfiguration,
            @AgentClass Class<? extends Agent> agentClass) {
        AgentConfiguration<? extends UkOpenBankingClientConfigurationAdapter> configuration =
                getAgentConfiguration(context);

        EidasIdentity eidasIdentity = createEidasIdentity(context, agentClass);
        return new UkOpenBankingFlowFacade(
                createEidasProxyTlsConfigurationSetter(agentsServiceConfiguration),
                createJwtSigner(
                        getAgentConfiguration(context).getProviderSpecificConfiguration(),
                        agentComponentProvider,
                        agentsServiceConfiguration,
                        eidasIdentity),
                configuration,
                eidasIdentity);
    }

    private JwtSigner createJwtSigner(
            UkOpenBankingClientConfigurationAdapter configuration,
            AgentComponentProvider agentComponentProvider,
            AgentsServiceConfiguration agentsServiceConfiguration,
            EidasIdentity eidasIdentity) {
        return new EidasProxyJwtSignerProvider(
                        configuration,
                        agentComponentProvider,
                        agentsServiceConfiguration,
                        CONFIGURATION_CLASS,
                        eidasIdentity)
                .get();
    }

    private TlsConfigurationSetter createEidasProxyTlsConfigurationSetter(
            AgentsServiceConfiguration configuration) {
        return new EidasProxyTlsConfigurationSetter(configuration);
    }

    private EidasIdentity createEidasIdentity(
            CompositeAgentContext context, Class<? extends Agent> agentClass) {
        return new EidasIdentity(
                context.getClusterId(),
                context.getAppId(),
                context.getCertId(),
                context.getProviderId(),
                agentClass);
    }

    private AgentConfiguration<AibClientConfiguration> getAgentConfiguration(
            CompositeAgentContext context) {
        return context.getAgentConfigurationController().getAgentConfiguration(CONFIGURATION_CLASS);
    }
}
