package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.module.agentclass.AgentClass;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingFlowFacade;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.configuration.UkOpenBankingClientConfigurationAdapter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.configuration.UkOpenBankingConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.tls.FakeTlsConfigurationSetter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.fake.FakeJwtSigner;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;

public class UkOpenBankingLocalKeySignerModuleForDecoupledMode extends AbstractModule {
    private static final Class<UkOpenBankingConfiguration> CONFIGURATION_CLASS =
            UkOpenBankingConfiguration.class;

    @Inject
    @Provides
    public UkOpenBankingFlowFacade fakeUkOpenBankingFlow(
            CompositeAgentContext context, @AgentClass Class<? extends Agent> agentClass) {
        AgentConfiguration<? extends UkOpenBankingClientConfigurationAdapter> configuration =
                agentConfiguration(context);
        return new UkOpenBankingFlowFacade(
                tlsConfigurationSetter(),
                fakeJwtSigner(),
                configuration,
                eidasIdentity(context, agentClass));
    }

    private FakeJwtSigner fakeJwtSigner() {
        return new FakeJwtSigner();
    }

    private FakeTlsConfigurationSetter tlsConfigurationSetter() {
        return new FakeTlsConfigurationSetter();
    }

    private AgentConfiguration<? extends UkOpenBankingClientConfigurationAdapter>
            agentConfiguration(CompositeAgentContext context) {
        return context.getAgentConfigurationController().getAgentConfiguration(CONFIGURATION_CLASS);
    }

    private EidasIdentity eidasIdentity(
            CompositeAgentContext context, Class<? extends Agent> agentClass) {
        return new EidasIdentity(context.getClusterId(), context.getAppId(), "UKOB", agentClass);
    }
}
