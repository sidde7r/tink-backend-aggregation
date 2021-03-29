package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.SneakyThrows;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.kid.EidasKidProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.kid.KidProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.signer.EidasJwsSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.signer.EidasProxyJwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public class UkOpenBankingEidasSignerModule extends AbstractModule {

    @Provides
    @Singleton
    @Inject
    public JwtSigner jwtSigner(
            AgentComponentProvider agentComponentProvider,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        CompositeAgentContext context = agentComponentProvider.getContext();
        EidasIdentity eidasIdentity = createEidasIdentity(context);
        EidasJwsSigner eidasJwsSigner =
                createEidasJwsSigner(eidasIdentity, agentsServiceConfiguration);
        KidProvider kidProvider = createKidProvider(context);
        return new EidasProxyJwtSigner(kidProvider, eidasJwsSigner);
    }

    private EidasIdentity createEidasIdentity(CompositeAgentContext context) {
        return new EidasIdentity(context.getClusterId(), context.getAppId(), "DEFAULT", "");
    }

    private EidasJwsSigner createEidasJwsSigner(
            EidasIdentity eidasIdentity, AgentsServiceConfiguration agentsServiceConfiguration) {
        return new EidasJwsSigner(
                agentsServiceConfiguration.getEidasProxy().toInternalConfig(), eidasIdentity);
    }

    @SneakyThrows
    private KidProvider createKidProvider(CompositeAgentContext context) {
        return new EidasKidProvider(context.getAgentConfigurationController().getQsealc());
    }
}
