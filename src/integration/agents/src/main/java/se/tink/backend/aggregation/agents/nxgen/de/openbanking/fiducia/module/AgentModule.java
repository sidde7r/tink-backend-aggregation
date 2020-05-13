package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.module;

import com.google.inject.AbstractModule;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.module.QSealcSignerModule;

public final class AgentModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new QSealcSignerModule(QsealcAlg.EIDAS_RSA_SHA256));
    }
}
