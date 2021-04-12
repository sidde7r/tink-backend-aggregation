package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.revolut;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.module.agentclass.AgentClass;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingFlowFacade;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.configuration.UkOpenBankingClientConfigurationAdapter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.kid.EidasKeyIdProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.kid.KeyIdProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.signer.EidasJwsSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.signer.EidasProxyJwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.tls.EidasProxyTlsConfigurationSetter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.tls.TlsConfigurationSetter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public class RevolutEUModule extends AbstractModule {

    private static final Class<RevolutEEAClientConfiguration> CONFIGURATION_CLASS =
            RevolutEEAClientConfiguration.class;

    @Inject
    @Singleton
    @Provides
    public UkOpenBankingFlowFacade revolutFlowFacade(
            CompositeAgentContext context,
            AgentComponentProvider agentComponentProvider,
            AgentsServiceConfiguration agentsServiceConfiguration,
            @AgentClass Class<? extends Agent> agentClass) {
        AgentConfiguration<? extends UkOpenBankingClientConfigurationAdapter> configuration =
                getAgentConfiguration(context);

        EidasIdentity eidasIdentity = createEidasIdentity(context, agentClass);
        return new UkOpenBankingFlowFacade(
                createEidasProxyTlsConfigurationSetter(agentsServiceConfiguration),
                jwtSigner(agentComponentProvider, agentsServiceConfiguration, eidasIdentity),
                configuration,
                eidasIdentity);
    }

    private JwtSigner jwtSigner(
            AgentComponentProvider agentComponentProvider,
            AgentsServiceConfiguration agentsServiceConfiguration,
            EidasIdentity eidasIdentity) {
        CompositeAgentContext context = agentComponentProvider.getContext();
        EidasJwsSigner eidasJwsSigner =
                createEidasJwsSigner(eidasIdentity, agentsServiceConfiguration);
        KeyIdProvider keyIdProvider = createKidProvider(context);
        return new EidasProxyJwtSigner(keyIdProvider, eidasJwsSigner);
    }

    private TlsConfigurationSetter createEidasProxyTlsConfigurationSetter(
            AgentsServiceConfiguration configuration) {
        return new EidasProxyTlsConfigurationSetter(configuration);
    }

    private EidasIdentity createEidasIdentity(
            CompositeAgentContext context, Class<? extends Agent> agentClass) {
        return new EidasIdentity(context.getClusterId(), context.getAppId(), agentClass);
    }

    private EidasJwsSigner createEidasJwsSigner(
            EidasIdentity eidasIdentity, AgentsServiceConfiguration agentsServiceConfiguration) {
        return new EidasJwsSigner(
                agentsServiceConfiguration.getEidasProxy().toInternalConfig(), eidasIdentity);
    }

    private KeyIdProvider createKidProvider(CompositeAgentContext context) {
        return new EidasKeyIdProvider(context.getAgentConfigurationController().getQsealc());
    }

    private AgentConfiguration<? extends UkOpenBankingClientConfigurationAdapter>
            getAgentConfiguration(CompositeAgentContext context) {
        return context.getAgentConfigurationController().getAgentConfiguration(CONFIGURATION_CLASS);
    }
}
