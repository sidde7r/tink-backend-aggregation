package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module;

import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingFlowFacade;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.configuration.UkOpenBankingClientConfigurationAdapter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.configuration.UkOpenBankingConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.signer.SecretServiceJwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.tls.LocalCertificateTlsConfigurationSetter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.tls.TlsConfigurationSetter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;

public class SecretServiceFlow implements UkOpenBankingFlowStrategy {

    private static final Class<UkOpenBankingConfiguration> CONFIGURATION_CLASS =
            UkOpenBankingConfiguration.class;
    private final CompositeAgentContext context;
    private final Class<? extends Agent> agentClass;

    public SecretServiceFlow(CompositeAgentContext context, Class<? extends Agent> agentClass) {
        this.context = context;
        this.agentClass = agentClass;
    }

    @Override
    public UkOpenBankingFlowFacade get() {
        AgentConfiguration<? extends UkOpenBankingClientConfigurationAdapter> configuration =
                agentConfiguration(context);
        UkOpenBankingConfiguration providerSpecificConfiguration =
                (UkOpenBankingConfiguration) configuration.getProviderSpecificConfiguration();
        return new UkOpenBankingFlowFacade(
                tlsConfigurationSetter(providerSpecificConfiguration),
                secretServiceJwtSigner(providerSpecificConfiguration),
                configuration,
                eidasIdentity());
    }

    private TlsConfigurationSetter tlsConfigurationSetter(
            UkOpenBankingConfiguration configuration) {
        return new LocalCertificateTlsConfigurationSetter(
                configuration.getTransportKey(), configuration.getTransportKeyPassword());
    }

    private JwtSigner secretServiceJwtSigner(UkOpenBankingConfiguration configuration) {
        return new SecretServiceJwtSigner(configuration);
    }

    private AgentConfiguration<? extends UkOpenBankingClientConfigurationAdapter>
            agentConfiguration(CompositeAgentContext context) {
        return context.getAgentConfigurationController().getAgentConfiguration(CONFIGURATION_CLASS);
    }

    private EidasIdentity eidasIdentity() {
        return new EidasIdentity(context.getClusterId(), context.getAppId(), "UKOB", agentClass);
    }
}
