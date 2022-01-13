package se.tink.backend.aggregation.nxgen.agents.componentproviders.eidas;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import se.tink.agent.sdk.utils.signer.qsealc.QsealcSigner;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.module.agentclass.AgentClass;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidasidentity.identity.EidasIdentity;
import se.tink.backend.aggregation.eidassigner.QsealcSignerImpl;

@Slf4j
public class ProductionQSealcSignerProvider implements QSealcSignerProvider {

    private final QsealcSignerImpl qsealcSigner;

    @Inject
    public ProductionQSealcSignerProvider(
            AgentsServiceConfiguration agentsServiceConfiguration,
            AgentContext agentContext,
            @AgentClass Class agentClass) {
        EidasProxyConfiguration eidasProxyConfiguration =
                agentsServiceConfiguration.getEidasProxy();
        EidasIdentity eidasIdentity = buildEidasIdentity(agentContext, agentClass);
        this.qsealcSigner =
                QsealcSignerImpl.build(eidasProxyConfiguration.toInternalConfig(), eidasIdentity);
    }

    private EidasIdentity buildEidasIdentity(AgentContext agentContext, Class<?> agentClass) {
        EidasIdentity eidasIdentity =
                new EidasIdentity(
                        agentContext.getClusterId(),
                        agentContext.getAppId(),
                        agentContext.getCertId(),
                        agentContext.getProviderId(),
                        agentClass);
        log.info("Eidas Identity setting: `{}`", eidasIdentity);
        return eidasIdentity;
    }

    @Override
    public QsealcSigner getQsealcSigner() {
        return this.qsealcSigner;
    }
}
