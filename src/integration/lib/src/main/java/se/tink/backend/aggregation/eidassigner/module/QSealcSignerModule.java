package se.tink.backend.aggregation.eidassigner.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.module.agentclass.AgentClass;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.QsealcSignerImpl;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;

public final class QSealcSignerModule extends AbstractModule {

    private final QsealcAlg qsealcAlg;

    public QSealcSignerModule(QsealcAlg qsealcAlg) {
        this.qsealcAlg = qsealcAlg;
    }

    @Singleton
    @Provides
    public QsealcSigner provideQSealcSigner(
            EidasProxyConfiguration configuration, EidasIdentity eidasIdentity) {
        return QsealcSignerImpl.build(configuration.toInternalConfig(), qsealcAlg, eidasIdentity);
    }

    @Singleton
    @Provides
    public EidasIdentity provideEidasIdentity(
            AgentContext agentContext, @AgentClass Class agentClass) {
        return new EidasIdentity(agentContext.getClusterId(), agentContext.getAppId(), agentClass);
    }
}
