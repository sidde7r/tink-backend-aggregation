package se.tink.backend.aggregation.eidassigner.module;

import com.google.inject.AbstractModule;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;

public final class QSealcSignerModuleRSASHA256 extends AbstractModule {

    @Override
    protected void configure() {
        install(new QSealcSignerModule(QsealcAlg.EIDAS_RSA_SHA256));
    }
}
