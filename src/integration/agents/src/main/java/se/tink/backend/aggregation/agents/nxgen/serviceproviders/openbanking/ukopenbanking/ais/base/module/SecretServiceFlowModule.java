package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingFlowFacade;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.configuration.UkOpenBankingClientConfigurationAdapter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.configuration.UkOpenBankingConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.signer.SecretServiceJwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.tls.LocalCertificateTlsConfigurationSetter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.tls.TlsConfigurationSetter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;

public class SecretServiceFlowModule extends AbstractModule {

    private static final Class<UkOpenBankingConfiguration> CONFIGURATION_CLASS =
            UkOpenBankingConfiguration.class;

    @Provides
    @Singleton
    @Inject
    public UkOpenBankingFlowFacade ukOpenBankingFlowFacade(CompositeAgentContext context) {
        AgentConfiguration<? extends UkOpenBankingClientConfigurationAdapter> configuration =
                agentConfiguration(context);
        UkOpenBankingConfiguration providerSpecificConfiguration =
                (UkOpenBankingConfiguration) configuration.getProviderSpecificConfiguration();
        return new UkOpenBankingFlowFacade(
                tlsConfigurationSetter(providerSpecificConfiguration),
                secretServiceJwtSigner(providerSpecificConfiguration),
                configuration);
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
}
