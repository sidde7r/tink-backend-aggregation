package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module;

import com.google.inject.Inject;
import com.google.inject.Provider;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingFlowFacade;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.configuration.UkOpenBankingClientConfigurationAdapter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.configuration.UkOpenBankingConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.signer.EidasProxyJwtSignerProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.tls.EidasProxyTlsConfigurationSetter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.tls.TlsConfigurationSetter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public class EidasProxyFlow
        implements UkOpenBankingFlowStrategy, Provider<UkOpenBankingFlowFacade> {

    private static final Class<UkOpenBankingConfiguration> CONFIGURATION_CLASS =
            UkOpenBankingConfiguration.class;
    private final CompositeAgentContext context;
    private final AgentComponentProvider agentComponentProvider;
    private final AgentsServiceConfiguration agentsServiceConfiguration;
    private final EidasIdentity eidasIdentity;

    @Inject
    public EidasProxyFlow(
            CompositeAgentContext context,
            AgentComponentProvider agentComponentProvider,
            AgentsServiceConfiguration agentsServiceConfiguration,
            EidasIdentity eidasIdentity) {
        this.context = context;
        this.agentComponentProvider = agentComponentProvider;
        this.agentsServiceConfiguration = agentsServiceConfiguration;
        this.eidasIdentity = eidasIdentity;
    }

    @Override
    public UkOpenBankingFlowFacade get() {
        AgentConfiguration<? extends UkOpenBankingClientConfigurationAdapter> configuration =
                getAgentConfiguration(context);

        return new UkOpenBankingFlowFacade(
                createEidasProxyTlsConfigurationSetter(agentsServiceConfiguration),
                createJwtSigner(
                        configuration.getProviderSpecificConfiguration(),
                        agentComponentProvider,
                        agentsServiceConfiguration),
                configuration,
                eidasIdentity);
    }

    private TlsConfigurationSetter createEidasProxyTlsConfigurationSetter(
            AgentsServiceConfiguration configuration) {
        return new EidasProxyTlsConfigurationSetter(configuration);
    }

    private JwtSigner createJwtSigner(
            UkOpenBankingClientConfigurationAdapter configuration,
            AgentComponentProvider agentComponentProvider,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        return new EidasProxyJwtSignerProvider(
                        configuration,
                        agentComponentProvider,
                        agentsServiceConfiguration,
                        CONFIGURATION_CLASS,
                        eidasIdentity)
                .get();
    }

    private AgentConfiguration<? extends UkOpenBankingClientConfigurationAdapter>
            getAgentConfiguration(CompositeAgentContext context) {
        return context.getAgentConfigurationController().getAgentConfiguration(CONFIGURATION_CLASS);
    }
}
